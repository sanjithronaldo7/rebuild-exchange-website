package com.rebuildexchange.controller;

import com.rebuildexchange.dto.ApiResponse;
import com.rebuildexchange.dto.DashboardStatsDTO;
import com.rebuildexchange.security.UserPrincipal;
import com.rebuildexchange.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Dashboards & Analytics", description = "Endpoints for environmental impact, seller hub snapshot, and admin metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/impact")
    @Operation(summary = "Get community-wide circular impact statistics (waste diverted kg, materials rehomed, connections)")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getImpactStats() {
        DashboardStatsDTO stats = dashboardService.getCommunityImpactStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/dashboard/seller")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get seller hub metrics for current authenticated seller")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSellerStats(@AuthenticationPrincipal UserPrincipal currentUser) {
        Map<String, Object> stats = dashboardService.getSellerHubStats(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get platform-wide administrative statistics (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        Map<String, Object> stats = dashboardService.getAdminStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
