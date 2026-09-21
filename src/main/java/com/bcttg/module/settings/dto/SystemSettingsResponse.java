package com.bcttg.module.settings.dto;

import java.time.Instant;

public class SystemSettingsResponse {
    private final String systemName;
    private final String systemDescription;
    private final String timezone;
    private final String language;
    private final Integer recordsPerPage;
    private final Boolean showAvatar;
    private final Boolean compactMode;
    private final Integer passwordMinLength;
    private final Boolean requireUppercase;
    private final Boolean requireNumber;
    private final Boolean requireSpecialChar;
    private final Integer sessionTimeout;
    private final Integer maxLoginAttempts;
    private final Boolean require2fa;
    private final String smtpHost;
    private final Integer smtpPort;
    private final String smtpUser;
    private final Boolean smtpPassConfigured;
    private final String smtpPassMasked;
    private final String emailFrom;
    private final Boolean notifyNewLogin;
    private final Boolean notifyPendingContent;
    private final Boolean notifySecurityAlerts;
    private final Boolean notifyPeriodicReports;
    private final Boolean autoBackupEnabled;
    private final String backupFrequency;
    private final Integer backupRetention;
    private final String version;
    private final Instant updatedAt;
    private final String updatedBy;

    public SystemSettingsResponse(
        String systemName,
        String systemDescription,
        String timezone,
        String language,
        Integer recordsPerPage,
        Boolean showAvatar,
        Boolean compactMode,
        Integer passwordMinLength,
        Boolean requireUppercase,
        Boolean requireNumber,
        Boolean requireSpecialChar,
        Integer sessionTimeout,
        Integer maxLoginAttempts,
        Boolean require2fa,
        String smtpHost,
        Integer smtpPort,
        String smtpUser,
        Boolean smtpPassConfigured,
        String smtpPassMasked,
        String emailFrom,
        Boolean notifyNewLogin,
        Boolean notifyPendingContent,
        Boolean notifySecurityAlerts,
        Boolean notifyPeriodicReports,
        Boolean autoBackupEnabled,
        String backupFrequency,
        Integer backupRetention,
        String version,
        Instant updatedAt,
        String updatedBy
    ) {
        this.systemName = systemName;
        this.systemDescription = systemDescription;
        this.timezone = timezone;
        this.language = language;
        this.recordsPerPage = recordsPerPage;
        this.showAvatar = showAvatar;
        this.compactMode = compactMode;
        this.passwordMinLength = passwordMinLength;
        this.requireUppercase = requireUppercase;
        this.requireNumber = requireNumber;
        this.requireSpecialChar = requireSpecialChar;
        this.sessionTimeout = sessionTimeout;
        this.maxLoginAttempts = maxLoginAttempts;
        this.require2fa = require2fa;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUser = smtpUser;
        this.smtpPassConfigured = smtpPassConfigured;
        this.smtpPassMasked = smtpPassMasked;
        this.emailFrom = emailFrom;
        this.notifyNewLogin = notifyNewLogin;
        this.notifyPendingContent = notifyPendingContent;
        this.notifySecurityAlerts = notifySecurityAlerts;
        this.notifyPeriodicReports = notifyPeriodicReports;
        this.autoBackupEnabled = autoBackupEnabled;
        this.backupFrequency = backupFrequency;
        this.backupRetention = backupRetention;
        this.version = version;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getSystemDescription() {
        return systemDescription;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getRecordsPerPage() {
        return recordsPerPage;
    }

    public Boolean getShowAvatar() {
        return showAvatar;
    }

    public Boolean getCompactMode() {
        return compactMode;
    }

    public Integer getPasswordMinLength() {
        return passwordMinLength;
    }

    public Boolean getRequireUppercase() {
        return requireUppercase;
    }

    public Boolean getRequireNumber() {
        return requireNumber;
    }

    public Boolean getRequireSpecialChar() {
        return requireSpecialChar;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public Integer getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public Boolean getRequire2fa() {
        return require2fa;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public Boolean getSmtpPassConfigured() {
        return smtpPassConfigured;
    }

    public String getSmtpPassMasked() {
        return smtpPassMasked;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public Boolean getNotifyNewLogin() {
        return notifyNewLogin;
    }

    public Boolean getNotifyPendingContent() {
        return notifyPendingContent;
    }

    public Boolean getNotifySecurityAlerts() {
        return notifySecurityAlerts;
    }

    public Boolean getNotifyPeriodicReports() {
        return notifyPeriodicReports;
    }

    public Boolean getAutoBackupEnabled() {
        return autoBackupEnabled;
    }

    public String getBackupFrequency() {
        return backupFrequency;
    }

    public Integer getBackupRetention() {
        return backupRetention;
    }

    public String getVersion() {
        return version;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
