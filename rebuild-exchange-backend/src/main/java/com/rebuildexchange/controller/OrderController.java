package com.rebuildexchange.controller;

import com.rebuildexchange.dto.ApiResponse;
import com.rebuildexchange.dto.CreateOrderRequest;
import com.rebuildexchange.dto.OrderResponseDTO;
import com.rebuildexchange.dto.OrderTrackingDTO;
import com.rebuildexchange.dto.UpdateOrderStatusRequest;
import com.rebuildexchange.security.UserPrincipal;
import com.rebuildexchange.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders & Tracking", description = "Endpoints for order management, fulfillment, and tracking history")
@SecurityRequirement(name = "BearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create an order for an available material or accepted request")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        OrderResponseDTO order = orderService.createOrder(req, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Order created successfully", order), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order details by ID (buyer, seller, or admin)")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        OrderResponseDTO order = orderService.getOrderById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get orders placed by the currently authenticated buyer")
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderResponseDTO> orders = orderService.getMyOrders(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/seller-orders")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    @Operation(summary = "Get orders received by the currently authenticated seller")
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getSellerOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderResponseDTO> orders = orderService.getSellerOrders(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update order status (READY_FOR_PICKUP, COMPLETED, CANCELLED)")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        OrderResponseDTO updated = orderService.updateOrderStatus(id, req, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updated));
    }

    @GetMapping("/{id}/tracking")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get full audit tracking history for an order")
    public ResponseEntity<ApiResponse<List<OrderTrackingDTO>>> getOrderTracking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<OrderTrackingDTO> tracking = orderService.getOrderTracking(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(tracking));
    }
}
