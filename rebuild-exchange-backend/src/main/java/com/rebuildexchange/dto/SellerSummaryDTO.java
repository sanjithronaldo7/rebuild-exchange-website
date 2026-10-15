package com.rebuildexchange.dto;

import com.rebuildexchange.entity.User;

public class SellerSummaryDTO {

    private Long id;
    private String fullName;
    private String initial;
    private String avatarUrl;
    private String bio;

    public SellerSummaryDTO() {}

    public SellerSummaryDTO(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.initial = user.getFullName() != null && !user.getFullName().isEmpty()
                ? user.getFullName().substring(0, 1).toUpperCase()
                : "S";
        this.avatarUrl = user.getAvatarUrl();
        this.bio = user.getBio() != null ? user.getBio() : "Local community member · Verified listing";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
