package com.rebuildexchange.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerifyOtpRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 8, message = "OTP code must be between 6 and 8 characters")
    private String otpCode;

    private String purpose = "REGISTRATION"; // REGISTRATION, LOGIN

    public VerifyOtpRequest() {}

    public VerifyOtpRequest(String email, String otpCode, String purpose) {
        this.email = email;
        this.otpCode = otpCode;
        this.purpose = purpose;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
