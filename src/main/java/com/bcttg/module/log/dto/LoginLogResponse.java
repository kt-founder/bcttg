package com.bcttg.module.log.dto;

import java.time.Instant;

public class LoginLogResponse {
    private final Long id;
    private final Long userId;
    private final String userName;
    private final String unitName;
    private final String action;
    private final String ipAddress;
    private final String device;
    private final String status;
    private final String failureReason;
    private final Instant createdAt;

    public LoginLogResponse(
        Long id,
        Long userId,
        String userName,
        String unitName,
        String action,
        String ipAddress,
        String device,
        String status,
        String failureReason,
        Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.unitName = unitName;
        this.action = action;
        this.ipAddress = ipAddress;
        this.device = device;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getAction() {
        return action;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDevice() {
        return device;
    }

    public String getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
