package com.bcttg.module.user.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateUserActiveRequest {
    @NotNull
    private Boolean value;

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
