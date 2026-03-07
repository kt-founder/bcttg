package com.bcttg.module.content.dto;

import java.time.Instant;

import com.bcttg.module.content.entity.ContentItem;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.media.dto.MediaResponse;

public class ContentItemResponse {
    private final Long id;
    private final ContentType type;
    private final Long categoryId;
    private final String title;
    private final String summary;
    private final String bodyHtml;
    private final MediaResponse coverMedia;
    private final Boolean isVisible;
    private final Integer sortOrder;
    private final Instant publishedAt;
    private final Integer viewCount;
    private final Instant createdAt;
    private final Instant updatedAt;

    public ContentItemResponse(ContentItem item) {
        this.id = item.getId();
        this.type = item.getCategory() != null ? item.getCategory().getType() : null;
        this.categoryId = item.getCategory() != null ? item.getCategory().getId() : null;
        this.title = item.getTitle();
        this.summary = item.getSummary();
        this.bodyHtml = item.getBodyHtml();
        this.coverMedia = item.getCoverMedia() != null ? new MediaResponse(item.getCoverMedia()) : null;
        this.isVisible = item.getIsVisible();
        this.sortOrder = item.getSortOrder();
        this.publishedAt = item.getPublishedAt();
        this.viewCount = item.getViewCount();
        this.createdAt = item.getCreatedAt();
        this.updatedAt = item.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public ContentType getType() {
        return type;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public MediaResponse getCoverMedia() {
        return coverMedia;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
