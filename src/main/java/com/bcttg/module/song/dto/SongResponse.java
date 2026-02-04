package com.bcttg.module.song.dto;

import java.time.Instant;

import com.bcttg.module.media.dto.MediaResponse;
import com.bcttg.module.song.entity.Song;

public class SongResponse {
    private final Long id;
    private final Long categoryId;
    private final String title;
    private final String lyric;
    private final MediaResponse audioMedia;
    private final String audioUrl;
    private final Integer durationSec;
    private final Boolean isVisible;
    private final Integer sortOrder;
    private final Instant createdAt;
    private final Instant updatedAt;

    public SongResponse(Song song) {
        this.id = song.getId();
        this.categoryId = song.getCategory() != null ? song.getCategory().getId() : null;
        this.title = song.getTitle();
        this.lyric = song.getLyric();
        this.audioMedia = song.getAudioMedia() != null ? new MediaResponse(song.getAudioMedia()) : null;
        this.audioUrl = song.getAudioUrl();
        this.durationSec = song.getDurationSec();
        this.isVisible = song.getIsVisible();
        this.sortOrder = song.getSortOrder();
        this.createdAt = song.getCreatedAt();
        this.updatedAt = song.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getLyric() {
        return lyric;
    }

    public MediaResponse getAudioMedia() {
        return audioMedia;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
