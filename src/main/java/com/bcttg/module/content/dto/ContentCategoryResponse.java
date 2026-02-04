package com.bcttg.module.content.dto;

import java.time.Instant;

import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentType;

public class ContentCategoryResponse {
    private final Long id;
    private final ContentType type;
    private final Long parentId;
    private final String name;
    private final String slug;
    private final String description;
    private final Boolean isVisible;
    private final Integer sortOrder;
    private final Instant createdAt;
    private final Instant updatedAt;

    public ContentCategoryResponse(ContentCategory category) {
        this.id = category.getId();
        this.type = category.getType();
        this.parentId = category.getParent() != null ? category.getParent().getId() : null;
        this.name = category.getName();
        this.slug = category.getSlug();
        this.description = category.getDescription();
        this.isVisible = category.getIsVisible();
        this.sortOrder = category.getSortOrder();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public ContentType getType() {
        return type;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
