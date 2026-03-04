package com.bcttg.module.profile.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.bcttg.module.media.dto.MediaResponse;
import com.bcttg.module.profile.entity.DataProfile;
import com.bcttg.module.profile.entity.ProfileType;

public class DataProfileResponse {
    private final Long id;
    private final ProfileType profileType;
    private final String fullName;
    private final String position;
    private final String unitName;
    private final String rankName;
    private final String heroTitle;
    private final String contactPhone;
    private final LocalDate birthDate;
    private final String hometown;
    private final String summary;
    private final String biography;
    private final String achievements;
    private final MediaResponse avatarMedia;
    private final Boolean isVisible;
    private final Integer sortOrder;
    private final String createdByPhone;
    private final Instant createdAt;
    private final Instant updatedAt;

    public DataProfileResponse(DataProfile profile) {
        this.id = profile.getId();
        this.profileType = profile.getProfileType();
        this.fullName = profile.getFullName();
        this.position = profile.getPosition();
        this.unitName = profile.getUnitName();
        this.rankName = profile.getRankName();
        this.heroTitle = profile.getHeroTitle();
        this.contactPhone = profile.getContactPhone();
        this.birthDate = profile.getBirthDate();
        this.hometown = profile.getHometown();
        this.summary = profile.getSummary();
        this.biography = profile.getBiography();
        this.achievements = profile.getAchievements();
        this.avatarMedia = profile.getAvatarMedia() != null ? new MediaResponse(profile.getAvatarMedia()) : null;
        this.isVisible = profile.getIsVisible();
        this.sortOrder = profile.getSortOrder();
        this.createdByPhone = profile.getCreatedByPhone();
        this.createdAt = profile.getCreatedAt();
        this.updatedAt = profile.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPosition() {
        return position;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getRankName() {
        return rankName;
    }

    public String getHeroTitle() {
        return heroTitle;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getHometown() {
        return hometown;
    }

    public String getSummary() {
        return summary;
    }

    public String getBiography() {
        return biography;
    }

    public String getAchievements() {
        return achievements;
    }

    public MediaResponse getAvatarMedia() {
        return avatarMedia;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getCreatedByPhone() {
        return createdByPhone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
