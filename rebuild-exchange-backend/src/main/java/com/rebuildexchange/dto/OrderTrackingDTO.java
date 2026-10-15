package com.rebuildexchange.dto;

import com.rebuildexchange.entity.OrderTracking;
import java.time.LocalDateTime;

public class OrderTrackingDTO {

    private Long id;
    private String previousStatus;
    private String newStatus;
    private String notes;
    private String updatedByUserName;
    private LocalDateTime timestamp;

    public OrderTrackingDTO() {}

    public OrderTrackingDTO(OrderTracking tracking) {
        this.id = tracking.getId();
        this.previousStatus = tracking.getPreviousStatus();
        this.newStatus = tracking.getNewStatus();
        this.notes = tracking.getNotes();
        if (tracking.getUpdatedByUser() != null) {
            this.updatedByUserName = tracking.getUpdatedByUser().getFullName();
        }
        this.timestamp = tracking.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUpdatedByUserName() {
        return updatedByUserName;
    }

    public void setUpdatedByUserName(String updatedByUserName) {
        this.updatedByUserName = updatedByUserName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
