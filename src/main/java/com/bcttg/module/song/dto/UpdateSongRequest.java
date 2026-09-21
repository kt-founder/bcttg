package com.bcttg.module.song.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class UpdateSongRequest {
    private Long categoryId;
    @Pattern(regexp = ".*\\S.*", message = "must not be blank")
    private String title;
    @Pattern(regexp = ".*\\S.*", message = "must not be blank")
    private String author;
    @Min(0)
    private Integer releaseYear;
    @Pattern(regexp = ".*\\S.*", message = "must not be blank")
    private String lyric;
    private Long audioMediaId;
    private String audioUrl;
    private Integer durationSec;
    private Boolean isVisible;
    @Min(0)
    private Integer sortOrder;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public Long getAudioMediaId() {
        return audioMediaId;
    }

    public void setAudioMediaId(Long audioMediaId) {
        this.audioMediaId = audioMediaId;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(Integer durationSec) {
        this.durationSec = durationSec;
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
