package com.rebuildexchange.service;

import com.rebuildexchange.dto.PaymentResponseDTO;
import com.rebuildexchange.dto.VerifyPaymentRequest;
import com.rebuildexchange.entity.Order;
import com.rebuildexchange.entity.OrderTracking;
import com.rebuildexchange.entity.Payment;
import com.rebuildexchange.exception.BadRequestException;
import com.rebuildexchange.exception.ResourceNotFoundException;
import com.rebuildexchange.repository.OrderRepository;
import com.rebuildexchange.repository.OrderTrackingRepository;
import com.rebuildexchange.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderTrackingRepository trackingRepository;

    @Value("${app.razorpay.key-id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Value("${app.razorpay.key-secret:placeholder_secret}")
    private String razorpayKeySecret;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          OrderTrackingRepository trackingRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.trackingRepository = trackingRepository;
    }

    @Transactional
    public PaymentResponseDTO createPaymentOrder(Long orderId, Long authenticatedBuyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getBuyer().getId().equals(authenticatedBuyerId)) {
            throw new BadRequestException("You can only pay for your own orders");
        }

        if ("PAID".equalsIgnoreCase(order.getOrderStatus()) || "COMPLETED".equalsIgnoreCase(order.getOrderStatus())) {
            throw new BadRequestException("Order is already paid or completed");
        }

        String paymentRef = "PAY-RBX-" + System.currentTimeMillis();
        String gatewayOrderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentReference(paymentRef);
        payment.setGatewayOrderId(gatewayOrderId);
        payment.setPaymentProvider("RAZORPAY");
        payment.setAmount(order.getAgreedPrice());
        payment.setCurrency("INR");
        payment.setPaymentStatus("INITIATED");

        paymentRepository.save(payment);

        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setOrderId(order.getId());
        response.setPaymentReference(paymentRef);
        response.setGatewayOrderId(gatewayOrderId);
        response.setAmount(order.getAgreedPrice());
        response.setCurrency("INR");
        response.setRazorpayKeyId(razorpayKeyId);
        response.setPaymentStatus("INITIATED");

        return response;
    }

    @Transactional
    public boolean verifyPaymentSignature(VerifyPaymentRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + req.getOrderId()));

        Payment payment = paymentRepository.findByGatewayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for gateway order: " + req.getRazorpayOrderId()));

        String payload = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();
        String expectedSignature = calculateHmacSha256(payload, razorpayKeySecret);

        boolean isValid = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                req.getRazorpaySignature().getBytes(StandardCharsets.UTF_8)
        );

        if (!isValid) {
            payment.setPaymentStatus("FAILED");
            payment.setErrorMessage("HMAC-SHA256 signature verification failed");
            paymentRepository.save(payment);
            throw new BadRequestException("Payment verification failed: Invalid signature");
        }

        // Signature is verified by server
        payment.setGatewayPaymentId(req.getRazorpayPaymentId());
        payment.setGatewaySignature(req.getRazorpaySignature());
        payment.setPaymentStatus("SUCCESS");
        paymentRepository.save(payment);

        // Update order status strictly upon verified payment
        String previousStatus = order.getOrderStatus();
        order.setOrderStatus("PAID");
        orderRepository.save(order);

        // Record tracking audit
        OrderTracking tracking = new OrderTracking(
                order,
                previousStatus,
                "PAID",
                "Payment of ₹" + payment.getAmount() + " successfully verified (Payment ID: " + req.getRazorpayPaymentId() + ")",
                order.getBuyer()
        );
        trackingRepository.save(tracking);

        logger.info("Successfully verified payment {} for order {}", req.getRazorpayPaymentId(), order.getId());
        return true;
    }

    public String calculateHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : rawHmac) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", ex);
        }
    }
}
