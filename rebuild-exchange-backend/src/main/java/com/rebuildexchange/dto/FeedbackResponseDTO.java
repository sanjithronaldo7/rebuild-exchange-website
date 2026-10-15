package com.rebuildexchange.dto;

import com.rebuildexchange.entity.Feedback;
import java.time.LocalDateTime;

public class FeedbackResponseDTO {

    private Long id;
    private Long orderId;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public FeedbackResponseDTO() {}

    public FeedbackResponseDTO(Feedback feedback) {
        this.id = feedback.getId();
        if (feedback.getOrder() != null) {
            this.orderId = feedback.getOrder().getId();
        }
        if (feedback.getBuyer() != null) {
            this.buyerId = feedback.getBuyer().getId();
            this.buyerName = feedback.getBuyer().getFullName();
        }
        if (feedback.getSeller() != null) {
            this.sellerId = feedback.getSeller().getId();
            this.sellerName = feedback.getSeller().getFullName();
        }
        this.rating = feedback.getRating();
        this.comment = feedback.getComment();
        this.createdAt = feedback.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
