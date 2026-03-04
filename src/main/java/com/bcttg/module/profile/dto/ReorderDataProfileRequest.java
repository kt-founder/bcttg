package com.bcttg.module.profile.dto;

import java.util.List;

import com.bcttg.module.profile.entity.ProfileType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ReorderDataProfileRequest {
    @NotNull
    private ProfileType profileType;

    @Valid
    @NotNull
    private List<OrderItem> orders;

    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public List<OrderItem> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderItem> orders) {
        this.orders = orders;
    }

    public static class OrderItem {
        @NotNull
        private Long id;

        @NotNull
        private Integer sortOrder;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}
