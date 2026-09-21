package com.bcttg.module.content.entity;

import com.bcttg.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "content_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_content_category_scope_slug", columnNames = {"type", "parent_id", "slug"})
    })
@SQLDelete(sql = "UPDATE content_categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class ContentCategory extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContentType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ContentCategory parent;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isVisible = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }

    public ContentCategory getParent() {
        return parent;
    }

    public void setParent(ContentCategory parent) {
        this.parent = parent;
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
