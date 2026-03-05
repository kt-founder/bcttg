package com.bcttg.module.user.dto;

import com.bcttg.module.user.entity.UserRole;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{8,15}$", message = "must contain only digits and have length 8-15")
    private String phone;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotNull
    private UserRole role;

    private Boolean isActive;

    @Valid
    @NotNull
    private UserProfilePayload profile;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public UserProfilePayload getProfile() {
        return profile;
    }

    public void setProfile(UserProfilePayload profile) {
        this.profile = profile;
    }
}
