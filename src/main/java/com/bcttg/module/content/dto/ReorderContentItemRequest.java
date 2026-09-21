package com.bcttg.module.content.dto;

import java.util.List;

import com.bcttg.module.content.entity.ContentType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ReorderContentItemRequest {
    private Long categoryId;
    private ContentType type;
    @Valid
    @NotNull
    private List<OrderItem> orders;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
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
