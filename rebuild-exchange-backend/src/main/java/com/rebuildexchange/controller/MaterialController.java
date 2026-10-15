package com.rebuildexchange.controller;

import com.rebuildexchange.dto.ApiResponse;
import com.rebuildexchange.dto.MaterialRequestDTO;
import com.rebuildexchange.dto.MaterialResponseDTO;
import com.rebuildexchange.security.UserPrincipal;
import com.rebuildexchange.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/materials")
@Tag(name = "Material Listings", description = "Endpoints for surplus construction material marketplace listings")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    @Operation(summary = "Search and filter materials with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<MaterialResponseDTO>>> getMaterials(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean freeOnly,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size) {

        Page<MaterialResponseDTO> results = materialService.searchMaterials(
                query, category, location, minPrice, maxPrice, freeOnly, condition, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/search")
    @Operation(summary = "Dedicated search endpoint for materials with query and category filters")
    public ResponseEntity<ApiResponse<Page<MaterialResponseDTO>>> searchMaterials(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean freeOnly,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size) {

        Page<MaterialResponseDTO> results = materialService.searchMaterials(
                query, category, location, minPrice, maxPrice, freeOnly, condition, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full details of a material listing by ID")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> getMaterialById(@PathVariable Long id) {
        MaterialResponseDTO material = materialService.getMaterialById(id);
        return ResponseEntity.ok(ApiResponse.success(material));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a new surplus material listing (Seller or Admin only)")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> createMaterial(
            @Valid @RequestBody MaterialRequestDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        MaterialResponseDTO created = materialService.createMaterial(dto, currentUser.getId());
        return new ResponseEntity<>(ApiResponse.success("Material listed successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update an existing material listing (Owner only)")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestBody MaterialRequestDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        MaterialResponseDTO updated = materialService.updateMaterial(id, dto, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Material updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Deactivate/delete a material listing (Owner only)")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        materialService.deleteMaterial(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Material listing removed successfully", null));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_SELLER', 'ROLE_ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Upload and attach an image to a material listing (Owner only)")
    public ResponseEntity<ApiResponse<MaterialResponseDTO>> uploadMaterialImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        MaterialResponseDTO response = materialService.uploadMaterialImage(id, file, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }

    @GetMapping("/my-listings")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get listings published by the currently authenticated seller")
    public ResponseEntity<ApiResponse<Page<MaterialResponseDTO>>> getMyListings(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MaterialResponseDTO> myListings = materialService.getMyListings(currentUser.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(myListings));
    }
}
