package com.bcttg.module.user.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.bcttg.module.user.entity.UserAccount;
import com.bcttg.module.user.entity.UserProfile;
import com.bcttg.module.user.entity.UserRole;

public class UserAdminResponse {
    private final Long id;
    private final String phone;
    private final UserRole role;
    private final Boolean isActive;
    private final Profile profile;
    private final Instant createdAt;
    private final Instant updatedAt;

    public UserAdminResponse(UserAccount account, UserProfile userProfile) {
        this.id = account.getId();
        this.phone = account.getPhone();
        this.role = account.getRole();
        this.isActive = account.getIsActive();
        this.profile = userProfile != null ? new Profile(userProfile) : null;
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Profile getProfile() {
        return profile;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class Profile {
        private final Long id;
        private final String fullName;
        private final String position;
        private final String unitName;
        private final String rankName;
        private final String email;
        private final String address;
        private final LocalDate birthDate;

        public Profile(UserProfile userProfile) {
            this.id = userProfile.getId();
            this.fullName = userProfile.getFullName();
            this.position = userProfile.getPosition();
            this.unitName = userProfile.getUnitName();
            this.rankName = userProfile.getRankName();
            this.email = userProfile.getEmail();
            this.address = userProfile.getAddress();
            this.birthDate = userProfile.getBirthDate();
        }

        public Long getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public String getPosition() {
            return position;
        }

        public String getUnitName() {
            return unitName;
        }

        public String getRankName() {
            return rankName;
        }

        public String getEmail() {
            return email;
        }

        public String getAddress() {
            return address;
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }
    }
}
