package com.rebuildexchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateRequestStatusDTO {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACCEPTED|REJECTED|CANCELLED", message = "Status must be ACCEPTED, REJECTED, or CANCELLED")
    private String status;

    private String responseNotes;

    public UpdateRequestStatusDTO() {}

    public UpdateRequestStatusDTO(String status, String responseNotes) {
        this.status = status;
        this.responseNotes = responseNotes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponseNotes() {
        return responseNotes;
    }

    public void setResponseNotes(String responseNotes) {
        this.responseNotes = responseNotes;
    }
}
