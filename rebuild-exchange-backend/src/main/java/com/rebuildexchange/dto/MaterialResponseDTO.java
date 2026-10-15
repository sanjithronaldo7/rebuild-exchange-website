package com.rebuildexchange.dto;

import com.rebuildexchange.entity.Material;
import com.rebuildexchange.entity.MaterialImage;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class MaterialResponseDTO {

    private Long id;
    private String title;
    private String category;
    private Long categoryId;
    private String quantity;
    private String unit;
    private String condition;
    private BigDecimal price;
    private Boolean isNegotiable;
    private String location;
    private String city;
    private String distance;
    private String image; // Primary image URL or unsplash ID
    private String description;
    private String seller;
    private Long sellerId;
    private String initial;
    private String time;
    private String availabilityStatus;
    private Integer viewCount;

    public MaterialResponseDTO() {}

    public MaterialResponseDTO(Material material) {
        this.id = material.getId();
        this.title = material.getTitle();
        this.category = material.getCategory() != null ? material.getCategory().getName() : "Other";
        this.categoryId = material.getCategory() != null ? material.getCategory().getId() : null;
        this.quantity = material.getQuantity();
        this.unit = material.getUnit();
        this.condition = material.getMaterialCondition();
        this.price = material.getPrice();
        this.isNegotiable = material.getIsNegotiable();
        this.location = material.getPickupLocation();
        this.city = material.getCity();
        this.distance = "Nearby";
        this.description = material.getDescription();
        this.availabilityStatus = material.getAvailabilityStatus();
        this.viewCount = material.getViewCount();

        // Primary image
        if (material.getImages() != null && !material.getImages().isEmpty()) {
            this.image = material.getImages().stream()
                    .filter(MaterialImage::getIsPrimary)
                    .findFirst()
                    .orElse(material.getImages().get(0))
                    .getImageUrl();
        } else {
            this.image = "photo-1504307651254-35680f356dfd";
        }

        // Seller summary
        if (material.getSeller() != null) {
            this.seller = material.getSeller().getFullName();
            this.sellerId = material.getSeller().getId();
            this.initial = material.getSeller().getFullName() != null && !material.getSeller().getFullName().isEmpty()
                    ? material.getSeller().getFullName().substring(0, 1).toUpperCase()
                    : "S";
        } else {
            this.seller = "Community Seller";
            this.initial = "C";
        }

        // Human-friendly relative time
        this.time = formatRelativeTime(material.getCreatedAt());
    }

    private String formatRelativeTime(LocalDateTime created) {
        if (created == null) return "Just now";
        Duration duration = Duration.between(created, LocalDateTime.now());
        long seconds = duration.getSeconds();
        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " minutes ago";
        long hours = minutes / 60;
        if (hours < 24) return hours == 1 ? "1 hour ago" : hours + " hours ago";
        long days = hours / 24;
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        return (days / 7) + " weeks ago";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getIsNegotiable() {
        return isNegotiable;
    }

    public void setIsNegotiable(Boolean isNegotiable) {
        this.isNegotiable = isNegotiable;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
}
