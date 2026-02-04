package com.bcttg.common;

import jakarta.validation.constraints.NotNull;

public class VisibilityRequest {
    @NotNull
    private Boolean isVisible;

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }
}
