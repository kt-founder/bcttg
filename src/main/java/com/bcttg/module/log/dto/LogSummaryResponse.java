package com.bcttg.module.log.dto;

public class LogSummaryResponse {
    private final long totalLogins;
    private final long successfulLogins;
    private final long failedLogins;
    private final long systemActions;

    public LogSummaryResponse(long totalLogins, long successfulLogins, long failedLogins, long systemActions) {
        this.totalLogins = totalLogins;
        this.successfulLogins = successfulLogins;
        this.failedLogins = failedLogins;
        this.systemActions = systemActions;
    }

    public long getTotalLogins() {
        return totalLogins;
    }

    public long getSuccessfulLogins() {
        return successfulLogins;
    }

    public long getFailedLogins() {
        return failedLogins;
    }

    public long getSystemActions() {
        return systemActions;
    }
}
