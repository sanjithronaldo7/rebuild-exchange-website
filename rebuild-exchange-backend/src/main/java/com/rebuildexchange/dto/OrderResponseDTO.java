package com.rebuildexchange.dto;

import com.rebuildexchange.entity.Order;
import com.rebuildexchange.entity.OrderTracking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private Long materialId;
    private String materialTitle;
    private String materialCategory;
    private String materialPickupLocation;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private BigDecimal agreedPrice;
    private String orderStatus;
    private LocalDateTime pickupScheduledTime;
    private LocalDateTime pickupCompletedTime;
    private String cancellationReason;
    private List<OrderTrackingDTO> trackingHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponseDTO() {}

    public OrderResponseDTO(Order order) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();
        if (order.getMaterial() != null) {
            this.materialId = order.getMaterial().getId();
            this.materialTitle = order.getMaterial().getTitle();
            this.materialCategory = order.getMaterial().getCategory() != null ? order.getMaterial().getCategory().getName() : "Other";
            this.materialPickupLocation = order.getMaterial().getPickupLocation();
        }
        if (order.getBuyer() != null) {
            this.buyerId = order.getBuyer().getId();
            this.buyerName = order.getBuyer().getFullName();
        }
        if (order.getSeller() != null) {
            this.sellerId = order.getSeller().getId();
            this.sellerName = order.getSeller().getFullName();
        }
        this.agreedPrice = order.getAgreedPrice();
        this.orderStatus = order.getOrderStatus();
        this.pickupScheduledTime = order.getPickupScheduledTime();
        this.pickupCompletedTime = order.getPickupCompletedTime();
        this.cancellationReason = order.getCancellationReason();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();

        if (order.getTrackingList() != null) {
            this.trackingHistory = order.getTrackingList().stream()
                    .map(OrderTrackingDTO::new)
                    .collect(Collectors.toList());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getMaterialTitle() {
        return materialTitle;
    }

    public void setMaterialTitle(String materialTitle) {
        this.materialTitle = materialTitle;
    }

    public String getMaterialCategory() {
        return materialCategory;
    }

    public void setMaterialCategory(String materialCategory) {
        this.materialCategory = materialCategory;
    }

    public String getMaterialPickupLocation() {
        return materialPickupLocation;
    }

    public void setMaterialPickupLocation(String materialPickupLocation) {
        this.materialPickupLocation = materialPickupLocation;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getPickupScheduledTime() {
        return pickupScheduledTime;
    }

    public void setPickupScheduledTime(LocalDateTime pickupScheduledTime) {
        this.pickupScheduledTime = pickupScheduledTime;
    }

    public LocalDateTime getPickupCompletedTime() {
        return pickupCompletedTime;
    }

    public void setPickupCompletedTime(LocalDateTime pickupCompletedTime) {
        this.pickupCompletedTime = pickupCompletedTime;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public List<OrderTrackingDTO> getTrackingHistory() {
        return trackingHistory;
    }

    public void setTrackingHistory(List<OrderTrackingDTO> trackingHistory) {
        this.trackingHistory = trackingHistory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
