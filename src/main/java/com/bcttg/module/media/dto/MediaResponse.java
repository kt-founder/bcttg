package com.bcttg.module.media.dto;

import java.time.Instant;

import com.bcttg.module.media.entity.MediaAsset;

public class MediaResponse {
    private final Long id;
    private final String fileName;
    private final String mimeType;
    private final Long sizeBytes;
    private final String url;
    private final String storageKey;
    private final String createdBy;
    private final Instant createdAt;

    public MediaResponse(MediaAsset media) {
        this.id = media.getId();
        this.fileName = media.getFileName();
        this.mimeType = media.getMimeType();
        this.sizeBytes = media.getSizeBytes();
        this.url = media.getUrl();
        this.storageKey = media.getStorageKey();
        this.createdBy = media.getCreatedBy();
        this.createdAt = media.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public String getUrl() {
        return url;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
