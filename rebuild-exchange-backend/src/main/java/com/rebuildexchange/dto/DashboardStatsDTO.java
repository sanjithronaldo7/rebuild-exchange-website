package com.rebuildexchange.dto;

import java.math.BigDecimal;

public class DashboardStatsDTO {

    private BigDecimal totalDivertedWeightKg;
    private long totalMaterialsRehomed;
    private long totalAvailableMaterials;
    private long totalActiveUsers;
    private long totalOrdersCompleted;
    private long totalLocalConnections;

    public DashboardStatsDTO() {}

    public BigDecimal getTotalDivertedWeightKg() {
        return totalDivertedWeightKg;
    }

    public void setTotalDivertedWeightKg(BigDecimal totalDivertedWeightKg) {
        this.totalDivertedWeightKg = totalDivertedWeightKg;
    }

    public long getTotalMaterialsRehomed() {
        return totalMaterialsRehomed;
    }

    public void setTotalMaterialsRehomed(long totalMaterialsRehomed) {
        this.totalMaterialsRehomed = totalMaterialsRehomed;
    }

    public long getTotalAvailableMaterials() {
        return totalAvailableMaterials;
    }

    public void setTotalAvailableMaterials(long totalAvailableMaterials) {
        this.totalAvailableMaterials = totalAvailableMaterials;
    }

    public long getTotalActiveUsers() {
        return totalActiveUsers;
    }

    public void setTotalActiveUsers(long totalActiveUsers) {
        this.totalActiveUsers = totalActiveUsers;
    }

    public long getTotalOrdersCompleted() {
        return totalOrdersCompleted;
    }

    public void setTotalOrdersCompleted(long totalOrdersCompleted) {
        this.totalOrdersCompleted = totalOrdersCompleted;
    }

    public long getTotalLocalConnections() {
        return totalLocalConnections;
    }

    public void setTotalLocalConnections(long totalLocalConnections) {
        this.totalLocalConnections = totalLocalConnections;
    }
}
