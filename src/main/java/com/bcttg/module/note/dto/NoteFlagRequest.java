package com.bcttg.module.note.dto;

import jakarta.validation.constraints.NotNull;

public class NoteFlagRequest {
    @NotNull
    private Boolean value;

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
