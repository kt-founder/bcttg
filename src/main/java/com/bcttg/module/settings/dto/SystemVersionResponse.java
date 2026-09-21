package com.bcttg.module.settings.dto;

import java.time.Instant;
import java.util.List;

public class SystemVersionResponse {
    private final String applicationName;
    private final String version;
    private final List<String> activeProfiles;
    private final String javaVersion;
    private final Instant startedAt;

    public SystemVersionResponse(
        String applicationName,
        String version,
        List<String> activeProfiles,
        String javaVersion,
        Instant startedAt
    ) {
        this.applicationName = applicationName;
        this.version = version;
        this.activeProfiles = activeProfiles;
        this.javaVersion = javaVersion;
        this.startedAt = startedAt;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getActiveProfiles() {
        return activeProfiles;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public Instant getStartedAt() {
        return startedAt;
    }
}
