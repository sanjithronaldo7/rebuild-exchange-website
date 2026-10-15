package com.rebuildexchange.dto;

import com.rebuildexchange.entity.PurchaseRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseRequestResponseDTO {

    private Long id;
    private Long materialId;
    private String materialTitle;
    private String materialCategory;
    private BigDecimal materialPrice;
    private String materialPickupLocation;
    private Long buyerId;
    private String buyerName;
    private String buyerEmail;
    private Long sellerId;
    private String sellerName;
    private String requestedQuantity;
    private BigDecimal offeredPrice;
    private String message;
    private String status; // PENDING, ACCEPTED, REJECTED, CANCELLED
    private String sellerResponseNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PurchaseRequestResponseDTO() {}

    public PurchaseRequestResponseDTO(PurchaseRequest req) {
        this.id = req.getId();
        if (req.getMaterial() != null) {
            this.materialId = req.getMaterial().getId();
            this.materialTitle = req.getMaterial().getTitle();
            this.materialCategory = req.getMaterial().getCategory() != null ? req.getMaterial().getCategory().getName() : "Other";
            this.materialPrice = req.getMaterial().getPrice();
            this.materialPickupLocation = req.getMaterial().getPickupLocation();
        }
        if (req.getBuyer() != null) {
            this.buyerId = req.getBuyer().getId();
            this.buyerName = req.getBuyer().getFullName();
            this.buyerEmail = req.getBuyer().getEmail();
        }
        if (req.getSeller() != null) {
            this.sellerId = req.getSeller().getId();
            this.sellerName = req.getSeller().getFullName();
        }
        this.requestedQuantity = req.getRequestedQuantity();
        this.offeredPrice = req.getOfferedPrice();
        this.message = req.getMessage();
        this.status = req.getStatus();
        this.sellerResponseNotes = req.getSellerResponseNotes();
        this.createdAt = req.getCreatedAt();
        this.updatedAt = req.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getMaterialPrice() {
        return materialPrice;
    }

    public void setMaterialPrice(BigDecimal materialPrice) {
        this.materialPrice = materialPrice;
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

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
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

    public String getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(String requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public BigDecimal getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(BigDecimal offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSellerResponseNotes() {
        return sellerResponseNotes;
    }

    public void setSellerResponseNotes(String sellerResponseNotes) {
        this.sellerResponseNotes = sellerResponseNotes;
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
