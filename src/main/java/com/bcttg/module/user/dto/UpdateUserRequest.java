package com.bcttg.module.user.dto;

import com.bcttg.module.user.entity.UserRole;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

public class UpdateUserRequest {
    @Pattern(regexp = "^[0-9]{8,15}$", message = "must contain only digits and have length 8-15")
    private String phone;

    private UserRole role;

    private Boolean isActive;

    @Valid
    private UpdateUserProfilePayload profile;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public UpdateUserProfilePayload getProfile() {
        return profile;
    }

    public void setProfile(UpdateUserProfilePayload profile) {
        this.profile = profile;
    }
}
