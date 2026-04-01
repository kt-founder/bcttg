package com.bcttg.module.settings.dto;

import java.time.Instant;

public class BackupFileResponse {
    private final String id;
    private final String fileName;
    private final long sizeBytes;
    private final Instant modifiedAt;
    private final boolean restorable;

    public BackupFileResponse(String id, String fileName, long sizeBytes, Instant modifiedAt, boolean restorable) {
        this.id = id;
        this.fileName = fileName;
        this.sizeBytes = sizeBytes;
        this.modifiedAt = modifiedAt;
        this.restorable = restorable;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public boolean isRestorable() {
        return restorable;
    }
}
