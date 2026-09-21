package com.bcttg.module.settings.dto;

import java.time.Instant;

public class OperationMessageResponse {
    private final String message;
    private final Instant executedAt;

    public OperationMessageResponse(String message, Instant executedAt) {
        this.message = message;
        this.executedAt = executedAt;
    }

    public String getMessage() {
        return message;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
