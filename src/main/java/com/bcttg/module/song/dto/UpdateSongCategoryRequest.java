package com.bcttg.module.song.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class UpdateSongCategoryRequest {
    private Long parentId;
    @Pattern(regexp = ".*\\S.*", message = "must not be blank")
    private String name;
    @Pattern(regexp = ".*\\S.*", message = "must not be blank")
    private String slug;
    private String description;
    private Boolean isVisible;
    @Min(0)
    private Integer sortOrder;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
