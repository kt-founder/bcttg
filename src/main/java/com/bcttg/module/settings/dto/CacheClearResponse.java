package com.bcttg.module.settings.dto;

import java.time.Instant;

public class CacheClearResponse {
    private final int clearedCaches;
    private final Instant clearedAt;
    private final String message;

    public CacheClearResponse(int clearedCaches, Instant clearedAt, String message) {
        this.clearedCaches = clearedCaches;
        this.clearedAt = clearedAt;
        this.message = message;
    }

    public int getClearedCaches() {
        return clearedCaches;
    }

    public Instant getClearedAt() {
        return clearedAt;
    }

    public String getMessage() {
        return message;
    }
}
