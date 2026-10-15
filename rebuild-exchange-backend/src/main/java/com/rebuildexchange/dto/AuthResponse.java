package com.rebuildexchange.dto;

import java.util.Set;

public class AuthResponse {

    private String tokenType = "Bearer";
    private String accessToken;
    private Long userId;
    private String fullName;
    private String email;
    private Set<String> roles;
    private boolean requiresOtp = false;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String accessToken, Long userId, String fullName, String email, Set<String> roles) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
        this.requiresOtp = false;
        this.message = "Authentication successful";
    }

    public static AuthResponse otpRequired(String email, String message) {
        AuthResponse res = new AuthResponse();
        res.setEmail(email);
        res.setRequiresOtp(true);
        res.setMessage(message);
        return res;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isRequiresOtp() {
        return requiresOtp;
    }

    public void setRequiresOtp(boolean requiresOtp) {
        this.requiresOtp = requiresOtp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
