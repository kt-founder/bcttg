package com.bcttg.module.log.dto;

import java.time.Instant;

public class SystemLogResponse {
    private final Long id;
    private final Long actorId;
    private final String actorName;
    private final String module;
    private final String action;
    private final String description;
    private final String level;
    private final String metadata;
    private final Instant createdAt;

    public SystemLogResponse(
        Long id,
        Long actorId,
        String actorName,
        String module,
        String action,
        String description,
        String level,
        String metadata,
        Instant createdAt
    ) {
        this.id = id;
        this.actorId = actorId;
        this.actorName = actorName;
        this.module = module;
        this.action = action;
        this.description = description;
        this.level = level;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getActorId() {
        return actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public String getLevel() {
        return level;
    }

    public String getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
