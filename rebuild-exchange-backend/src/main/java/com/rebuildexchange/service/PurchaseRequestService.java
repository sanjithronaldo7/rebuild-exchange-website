package com.rebuildexchange.service;

import com.rebuildexchange.dto.PurchaseRequestCreateDTO;
import com.rebuildexchange.dto.PurchaseRequestResponseDTO;
import com.rebuildexchange.dto.UpdateRequestStatusDTO;
import com.rebuildexchange.entity.Material;
import com.rebuildexchange.entity.Notification;
import com.rebuildexchange.entity.PurchaseRequest;
import com.rebuildexchange.entity.User;
import com.rebuildexchange.exception.BadRequestException;
import com.rebuildexchange.exception.ResourceNotFoundException;
import com.rebuildexchange.exception.UnauthorizedException;
import com.rebuildexchange.repository.MaterialRepository;
import com.rebuildexchange.repository.NotificationRepository;
import com.rebuildexchange.repository.PurchaseRequestRepository;
import com.rebuildexchange.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository requestRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public PurchaseRequestService(PurchaseRequestRepository requestRepository,
                                  MaterialRepository materialRepository,
                                  UserRepository userRepository,
                                  NotificationRepository notificationRepository) {
        this.requestRepository = requestRepository;
        this.materialRepository = materialRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public PurchaseRequestResponseDTO createPurchaseRequest(PurchaseRequestCreateDTO dto, Long authenticatedBuyerId) {
        User buyer = userRepository.findById(authenticatedBuyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + authenticatedBuyerId));

        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + dto.getMaterialId()));

        if (!"AVAILABLE".equalsIgnoreCase(material.getAvailabilityStatus())) {
            throw new BadRequestException("This material is currently not available for purchase requests");
        }

        User seller = material.getSeller();
        if (seller.getId().equals(buyer.getId())) {
            throw new BadRequestException("You cannot submit a purchase request for your own material listing");
        }

        PurchaseRequest request = new PurchaseRequest();
        request.setMaterial(material);
        request.setBuyer(buyer);
        request.setSeller(seller);
        request.setRequestedQuantity(dto.getRequestedQuantity());
        request.setOfferedPrice(dto.getOfferedPrice() != null ? dto.getOfferedPrice() : material.getPrice());
        request.setMessage(dto.getMessage());
        request.setStatus("PENDING");

        PurchaseRequest saved = requestRepository.save(request);

        // Notify seller
        Notification notif = new Notification(
                seller,
                "New Purchase Request Received",
                buyer.getFullName() + " has requested '" + material.getTitle() + "' (" + dto.getRequestedQuantity() + ").",
                "REQUEST_RECEIVED",
                String.valueOf(saved.getId())
        );
        notificationRepository.save(notif);

        return new PurchaseRequestResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseRequestResponseDTO> getMyPurchaseRequests(Long authenticatedBuyerId, Pageable pageable) {
        return requestRepository.findByBuyerId(authenticatedBuyerId, pageable)
                .map(PurchaseRequestResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseRequestResponseDTO> getSellerPurchaseRequests(Long authenticatedSellerId, Pageable pageable) {
        return requestRepository.findBySellerId(authenticatedSellerId, pageable)
                .map(PurchaseRequestResponseDTO::new);
    }

    @Transactional
    public PurchaseRequestResponseDTO updateRequestStatus(Long requestId, UpdateRequestStatusDTO dto, Long authenticatedSellerId) {
        PurchaseRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with id: " + requestId));

        if (!request.getSeller().getId().equals(authenticatedSellerId)) {
            throw new UnauthorizedException("You are not authorized to update this purchase request");
        }

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new BadRequestException("Cannot update a purchase request that is already " + request.getStatus());
        }

        request.setStatus(dto.getStatus().toUpperCase());
        if (dto.getResponseNotes() != null) {
            request.setSellerResponseNotes(dto.getResponseNotes());
        }

        PurchaseRequest updated = requestRepository.save(request);

        // Notify buyer of seller's decision
        String decisionText = "ACCEPTED".equalsIgnoreCase(dto.getStatus()) ? "accepted" : "declined";
        Notification notif = new Notification(
                request.getBuyer(),
                "Purchase Request " + dto.getStatus().toUpperCase(),
                request.getSeller().getFullName() + " has " + decisionText + " your request for '" + request.getMaterial().getTitle() + "'.",
                "REQUEST_" + dto.getStatus().toUpperCase(),
                String.valueOf(updated.getId())
        );
        notificationRepository.save(notif);

        return new PurchaseRequestResponseDTO(updated);
    }
}
