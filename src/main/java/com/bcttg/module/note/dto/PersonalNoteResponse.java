package com.bcttg.module.note.dto;

import java.time.Instant;

import com.bcttg.module.note.entity.PersonalNote;

public class PersonalNoteResponse {
    private final Long id;
    private final Long ownerUserId;
    private final String ownerPhone;
    private final String title;
    private final String content;
    private final String colorCode;
    private final Instant reminderAt;
    private final Boolean isPinned;
    private final Boolean isArchived;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PersonalNoteResponse(PersonalNote note) {
        this.id = note.getId();
        this.ownerUserId = note.getOwnerUser() != null ? note.getOwnerUser().getId() : null;
        this.ownerPhone = note.getOwnerUser() != null ? note.getOwnerUser().getPhone() : null;
        this.title = note.getTitle();
        this.content = note.getContent();
        this.colorCode = note.getColorCode();
        this.reminderAt = note.getReminderAt();
        this.isPinned = note.getIsPinned();
        this.isArchived = note.getIsArchived();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getColorCode() {
        return colorCode;
    }

    public Instant getReminderAt() {
        return reminderAt;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
