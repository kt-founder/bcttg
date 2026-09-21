package com.bcttg.module.settings.dto;

import java.time.Instant;

public class TestEmailResponse {
    private final String sentTo;
    private final Instant sentAt;
    private final String message;

    public TestEmailResponse(String sentTo, Instant sentAt, String message) {
        this.sentTo = sentTo;
        this.sentAt = sentAt;
        this.message = message;
    }

    public String getSentTo() {
        return sentTo;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String getMessage() {
        return message;
    }
}
