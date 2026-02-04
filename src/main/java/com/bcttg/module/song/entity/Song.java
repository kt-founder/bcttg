package com.bcttg.module.song.entity;

import com.bcttg.common.BaseEntity;
import com.bcttg.module.media.entity.MediaAsset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "songs")
@SQLDelete(sql = "UPDATE songs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Song extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SongCategory category;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String lyric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_media_id")
    private MediaAsset audioMedia;

    @Column(length = 1000)
    private String audioUrl;

    private Integer durationSec;

    @Column(nullable = false)
    private Boolean isVisible = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public SongCategory getCategory() {
        return category;
    }

    public void setCategory(SongCategory category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public MediaAsset getAudioMedia() {
        return audioMedia;
    }

    public void setAudioMedia(MediaAsset audioMedia) {
        this.audioMedia = audioMedia;
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
