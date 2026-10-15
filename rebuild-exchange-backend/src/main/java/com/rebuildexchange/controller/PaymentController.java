package com.rebuildexchange.controller;

import com.rebuildexchange.dto.ApiResponse;
import com.rebuildexchange.dto.CreatePaymentOrderRequest;
import com.rebuildexchange.dto.PaymentResponseDTO;
import com.rebuildexchange.dto.VerifyPaymentRequest;
import com.rebuildexchange.security.UserPrincipal;
import com.rebuildexchange.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Endpoints for Razorpay order generation and HMAC-SHA256 signature verification")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a gateway payment order for an existing order")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> createPaymentOrder(
            @Valid @RequestBody CreatePaymentOrderRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        PaymentResponseDTO response = paymentService.createPaymentOrder(req.getOrderId(), currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Payment order created successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Verify server-side HMAC-SHA256 signature for Razorpay payment")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(@Valid @RequestBody VerifyPaymentRequest req) {
        boolean verified = paymentService.verifyPaymentSignature(req);
        return ResponseEntity.ok(ApiResponse.success("Payment verified and order marked as PAID", verified));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay Webhook receiver for background payment notifications")
    public ResponseEntity<ApiResponse<String>> handleWebhook(@RequestBody Map<String, Object> payload) {
        // Idempotent webhook receiver
        return ResponseEntity.ok(ApiResponse.success("Webhook received", "OK"));
    }
}
