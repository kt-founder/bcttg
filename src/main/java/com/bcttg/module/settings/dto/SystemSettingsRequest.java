package com.bcttg.module.settings.dto;

import com.bcttg.module.settings.SystemSettingsWritable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SystemSettingsRequest implements SystemSettingsWritable {
    @NotBlank
    @Size(max = 200)
    private String systemName;

    private String systemDescription;

    @NotBlank
    @Size(max = 100)
    private String timezone;

    @NotBlank
    @Size(max = 20)
    private String language;

    @NotNull
    @Min(1)
    @Max(200)
    private Integer recordsPerPage;

    @NotNull
    private Boolean showAvatar;

    @NotNull
    private Boolean compactMode;

    @NotNull
    @Min(6)
    @Max(72)
    private Integer passwordMinLength;

    @NotNull
    private Boolean requireUppercase;

    @NotNull
    private Boolean requireNumber;

    @NotNull
    private Boolean requireSpecialChar;

    @NotNull
    @Min(5)
    @Max(1440)
    private Integer sessionTimeout;

    @NotNull
    @Min(1)
    @Max(20)
    private Integer maxLoginAttempts;

    @NotNull
    private Boolean require2fa;

    @Size(max = 200)
    private String smtpHost;

    @NotNull
    @Min(1)
    @Max(65535)
    private Integer smtpPort;

    @Size(max = 200)
    private String smtpUser;

    @Size(max = 255)
    private String smtpPass;

    @Size(max = 200)
    private String emailFrom;

    @NotNull
    private Boolean notifyNewLogin;

    @NotNull
    private Boolean notifyPendingContent;

    @NotNull
    private Boolean notifySecurityAlerts;

    @NotNull
    private Boolean notifyPeriodicReports;

    @NotNull
    private Boolean autoBackupEnabled;

    @NotBlank
    @Size(max = 20)
    private String backupFrequency;

    @NotNull
    @Min(1)
    @Max(365)
    private Integer backupRetention;

    @Override
    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public String getSystemDescription() {
        return systemDescription;
    }

    public void setSystemDescription(String systemDescription) {
        this.systemDescription = systemDescription;
    }

    @Override
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public Integer getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(Integer recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    @Override
    public Boolean getShowAvatar() {
        return showAvatar;
    }

    public void setShowAvatar(Boolean showAvatar) {
        this.showAvatar = showAvatar;
    }

    @Override
    public Boolean getCompactMode() {
        return compactMode;
    }

    public void setCompactMode(Boolean compactMode) {
        this.compactMode = compactMode;
    }

    @Override
    public Integer getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(Integer passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    @Override
    public Boolean getRequireUppercase() {
        return requireUppercase;
    }

    public void setRequireUppercase(Boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }

    @Override
    public Boolean getRequireNumber() {
        return requireNumber;
    }

    public void setRequireNumber(Boolean requireNumber) {
        this.requireNumber = requireNumber;
    }

    @Override
    public Boolean getRequireSpecialChar() {
        return requireSpecialChar;
    }

    public void setRequireSpecialChar(Boolean requireSpecialChar) {
        this.requireSpecialChar = requireSpecialChar;
    }

    @Override
    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public Integer getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(Integer maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }

    @Override
    public Boolean getRequire2fa() {
        return require2fa;
    }

    public void setRequire2fa(Boolean require2fa) {
        this.require2fa = require2fa;
    }

    @Override
    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    @Override
    public Integer getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    @Override
    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    @Override
    public String getSmtpPass() {
        return smtpPass;
    }

    public void setSmtpPass(String smtpPass) {
        this.smtpPass = smtpPass;
    }

    @Override
    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    @Override
    public Boolean getNotifyNewLogin() {
        return notifyNewLogin;
    }

    public void setNotifyNewLogin(Boolean notifyNewLogin) {
        this.notifyNewLogin = notifyNewLogin;
    }

    @Override
    public Boolean getNotifyPendingContent() {
        return notifyPendingContent;
    }

    public void setNotifyPendingContent(Boolean notifyPendingContent) {
        this.notifyPendingContent = notifyPendingContent;
    }

    @Override
    public Boolean getNotifySecurityAlerts() {
        return notifySecurityAlerts;
    }

    public void setNotifySecurityAlerts(Boolean notifySecurityAlerts) {
        this.notifySecurityAlerts = notifySecurityAlerts;
    }

    @Override
    public Boolean getNotifyPeriodicReports() {
        return notifyPeriodicReports;
    }

    public void setNotifyPeriodicReports(Boolean notifyPeriodicReports) {
        this.notifyPeriodicReports = notifyPeriodicReports;
    }

    @Override
    public Boolean getAutoBackupEnabled() {
        return autoBackupEnabled;
    }

    public void setAutoBackupEnabled(Boolean autoBackupEnabled) {
        this.autoBackupEnabled = autoBackupEnabled;
    }

    @Override
    public String getBackupFrequency() {
        return backupFrequency;
    }

    public void setBackupFrequency(String backupFrequency) {
        this.backupFrequency = backupFrequency;
    }

    @Override
    public Integer getBackupRetention() {
        return backupRetention;
    }

    public void setBackupRetention(Integer backupRetention) {
        this.backupRetention = backupRetention;
    }
}
