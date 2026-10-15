package com.rebuildexchange.service;

import com.rebuildexchange.dto.CreateOrderRequest;
import com.rebuildexchange.dto.OrderResponseDTO;
import com.rebuildexchange.dto.OrderTrackingDTO;
import com.rebuildexchange.dto.UpdateOrderStatusRequest;
import com.rebuildexchange.entity.*;
import com.rebuildexchange.exception.BadRequestException;
import com.rebuildexchange.exception.ResourceNotFoundException;
import com.rebuildexchange.exception.UnauthorizedException;
import com.rebuildexchange.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderTrackingRepository trackingRepository;
    private final MaterialRepository materialRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderTrackingRepository trackingRepository,
                        MaterialRepository materialRepository,
                        PurchaseRequestRepository purchaseRequestRepository,
                        UserRepository userRepository,
                        NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.trackingRepository = trackingRepository;
        this.materialRepository = materialRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequest req, Long authenticatedBuyerId) {
        User buyer = userRepository.findById(authenticatedBuyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + authenticatedBuyerId));

        Material material = materialRepository.findById(req.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + req.getMaterialId()));

        if (!"AVAILABLE".equalsIgnoreCase(material.getAvailabilityStatus())) {
            throw new BadRequestException("Material is no longer available for order");
        }

        PurchaseRequest pr = null;
        if (req.getPurchaseRequestId() != null) {
            pr = purchaseRequestRepository.findById(req.getPurchaseRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + req.getPurchaseRequestId()));
            if (!pr.getBuyer().getId().equals(buyer.getId())) {
                throw new UnauthorizedException("You can only create orders from your own purchase requests");
            }
        }

        BigDecimal agreedPrice = req.getAgreedPrice() != null ? req.getAgreedPrice() : material.getPrice();

        Order order = new Order();
        String orderNumber = "RBX-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        order.setOrderNumber(orderNumber);
        order.setMaterial(material);
        order.setBuyer(buyer);
        order.setSeller(material.getSeller());
        order.setPurchaseRequest(pr);
        order.setAgreedPrice(agreedPrice);

        // If price is zero (Free to collect), status is ACCEPTED/READY_FOR_PICKUP, otherwise PAYMENT_PENDING
        String initialStatus = agreedPrice.compareTo(BigDecimal.ZERO) == 0 ? "READY_FOR_PICKUP" : "PAYMENT_PENDING";
        order.setOrderStatus(initialStatus);

        Order saved = orderRepository.save(order);

        // Record initial tracking
        OrderTracking tracking = new OrderTracking(
                saved,
                null,
                initialStatus,
                req.getPickupNotes() != null ? req.getPickupNotes() : "Order created. Waiting for pickup/payment.",
                buyer
        );
        trackingRepository.save(tracking);

        // Notify seller
        Notification notif = new Notification(
                material.getSeller(),
                "New Order Created: " + orderNumber,
                buyer.getFullName() + " created order for '" + material.getTitle() + "'.",
                "ORDER_UPDATE",
                String.valueOf(saved.getId())
        );
        notificationRepository.save(notif);

        return new OrderResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId, Long authenticatedUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        boolean isParticipant = order.getBuyer().getId().equals(authenticatedUserId)
                || order.getSeller().getId().equals(authenticatedUserId);

        User currentUser = userRepository.findById(authenticatedUserId).orElseThrow();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isParticipant && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }

        return new OrderResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getMyOrders(Long buyerId, Pageable pageable) {
        return orderRepository.findByBuyerId(buyerId, pageable).map(OrderResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getSellerOrders(Long sellerId, Pageable pageable) {
        return orderRepository.findBySellerId(sellerId, pageable).map(OrderResponseDTO::new);
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest req, Long authenticatedUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        User currentUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authenticatedUserId));

        boolean isBuyer = order.getBuyer().getId().equals(authenticatedUserId);
        boolean isSeller = order.getSeller().getId().equals(authenticatedUserId);
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isBuyer && !isSeller && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to update this order status");
        }

        String currentStatus = order.getOrderStatus();
        String targetStatus = req.getNewStatus().toUpperCase();

        // Validate state transitions
        if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new BadRequestException("Cannot update an order that is already " + currentStatus);
        }

        if ("CANCELLED".equals(targetStatus)) {
            // Buyer or seller can cancel before completion
            order.setCancellationReason(req.getNotes() != null ? req.getNotes() : "Cancelled by user");
        } else if ("READY_FOR_PICKUP".equals(targetStatus)) {
            if (!isSeller && !isAdmin) {
                throw new UnauthorizedException("Only the seller or admin can mark order as READY_FOR_PICKUP");
            }
            order.setPickupScheduledTime(LocalDateTime.now());
        } else if ("COMPLETED".equals(targetStatus)) {
            if (!isSeller && !isAdmin) {
                throw new UnauthorizedException("Only the seller or admin can confirm pickup completion");
            }
            order.setPickupCompletedTime(LocalDateTime.now());

            // Mark material as REHOMED in MySQL
            Material material = order.getMaterial();
            material.setAvailabilityStatus("REHOMED");
            materialRepository.save(material);
        } else if ("PAID".equals(targetStatus)) {
            // Paid should only be set by verified payment gateway confirmation
            if (!isAdmin) {
                throw new BadRequestException("Orders cannot be manually marked as PAID without verified gateway confirmation");
            }
        }

        order.setOrderStatus(targetStatus);
        Order updated = orderRepository.save(order);

        // Record tracking audit
        OrderTracking tracking = new OrderTracking(
                updated,
                currentStatus,
                targetStatus,
                req.getNotes() != null ? req.getNotes() : "Order status updated to " + targetStatus,
                currentUser
        );
        trackingRepository.save(tracking);

        return new OrderResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<OrderTrackingDTO> getOrderTracking(Long orderId, Long authenticatedUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        boolean isParticipant = order.getBuyer().getId().equals(authenticatedUserId)
                || order.getSeller().getId().equals(authenticatedUserId);

        if (!isParticipant) {
            throw new UnauthorizedException("You are not authorized to view tracking for this order");
        }

        return trackingRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(OrderTrackingDTO::new)
                .collect(Collectors.toList());
    }
}
