package com.bcttg.module.settings.dto;

public class SystemStatusCardResponse {
    private final String id;
    private final String title;
    private final String value;
    private final String state;

    public SystemStatusCardResponse(String id, String title, String value, String state) {
        this.id = id;
        this.title = title;
        this.value = value;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getState() {
        return state;
    }
}
