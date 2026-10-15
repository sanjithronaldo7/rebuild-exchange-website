package com.rebuildexchange.controller;

import com.rebuildexchange.dto.ApiResponse;
import com.rebuildexchange.dto.PurchaseRequestCreateDTO;
import com.rebuildexchange.dto.PurchaseRequestResponseDTO;
import com.rebuildexchange.dto.UpdateRequestStatusDTO;
import com.rebuildexchange.security.UserPrincipal;
import com.rebuildexchange.service.PurchaseRequestService;
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

@RestController
@RequestMapping("/api/purchase-requests")
@Tag(name = "Purchase Requests", description = "Endpoints for buyer-seller material acquisition requests")
@SecurityRequirement(name = "BearerAuth")
public class PurchaseRequestController {

    private final PurchaseRequestService requestService;

    public PurchaseRequestController(PurchaseRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a purchase request for an available material")
    public ResponseEntity<ApiResponse<PurchaseRequestResponseDTO>> createPurchaseRequest(
            @Valid @RequestBody PurchaseRequestCreateDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        PurchaseRequestResponseDTO response = requestService.createPurchaseRequest(dto, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Purchase request submitted successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get purchase requests submitted by the current authenticated buyer")
    public ResponseEntity<ApiResponse<Page<PurchaseRequestResponseDTO>>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PurchaseRequestResponseDTO> requests = requestService.getMyPurchaseRequests(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/seller-requests")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    @Operation(summary = "Get incoming purchase requests for materials listed by the current authenticated seller")
    public ResponseEntity<ApiResponse<Page<PurchaseRequestResponseDTO>>> getSellerRequests(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PurchaseRequestResponseDTO> requests = requestService.getSellerPurchaseRequests(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    @Operation(summary = "Accept or reject an incoming purchase request (Seller only)")
    public ResponseEntity<ApiResponse<PurchaseRequestResponseDTO>> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestStatusDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        PurchaseRequestResponseDTO response = requestService.updateRequestStatus(id, dto, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Request status updated to " + dto.getStatus(), response));
    }
}
