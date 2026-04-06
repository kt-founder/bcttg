package com.bcttg.module.home.dto;

import java.time.Instant;

public class HomeModuleResponse {
    private final String id;
    private final String name;
    private final String description;
    private final Boolean enabled;
    private final Boolean isGuest;
    private final Integer sortOrder;
    private final Long itemCount;
    private final Instant updatedAt;

    public HomeModuleResponse(
        String id,
        String name,
        String description,
        Boolean enabled,
        Boolean isGuest,
        Integer sortOrder,
        Long itemCount,
        Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.isGuest = isGuest;
        this.sortOrder = sortOrder;
        this.itemCount = itemCount;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getIsGuest() {
        return isGuest;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Long getItemCount() {
        return itemCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
