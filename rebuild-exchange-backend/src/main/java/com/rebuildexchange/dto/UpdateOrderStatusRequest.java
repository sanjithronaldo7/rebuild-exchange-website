package com.rebuildexchange.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateOrderStatusRequest {

    @NotBlank(message = "New status is required")
    private String newStatus;

    private String notes;

    public UpdateOrderStatusRequest() {}

    public UpdateOrderStatusRequest(String newStatus, String notes) {
        this.newStatus = newStatus;
        this.notes = notes;
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
}
