package com.bcttg.module.settings.entity;

import com.bcttg.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "system_settings")
@SQLDelete(sql = "UPDATE system_settings SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class SystemSettings extends BaseEntity {
    @Column(nullable = false, length = 200)
    private String systemName;

    @Lob
    private String systemDescription;

    @Column(nullable = false, length = 100)
    private String timezone;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(nullable = false)
    private Integer recordsPerPage;

    @Column(nullable = false)
    private Boolean showAvatar;

    @Column(nullable = false)
    private Boolean compactMode;

    @Column(nullable = false)
    private Integer passwordMinLength;

    @Column(nullable = false)
    private Boolean requireUppercase;

    @Column(nullable = false)
    private Boolean requireNumber;

    @Column(nullable = false)
    private Boolean requireSpecialChar;

    @Column(nullable = false)
    private Integer sessionTimeout;

    @Column(nullable = false)
    private Integer maxLoginAttempts;

    @Column(nullable = false)
    private Boolean require2fa;

    @Column(length = 200)
    private String smtpHost;

    @Column(nullable = false)
    private Integer smtpPort;

    @Column(length = 200)
    private String smtpUser;

    @Column(length = 255)
    private String smtpPass;

    @Column(length = 200)
    private String emailFrom;

    @Column(nullable = false)
    private Boolean notifyNewLogin;

    @Column(nullable = false)
    private Boolean notifyPendingContent;

    @Column(nullable = false)
    private Boolean notifySecurityAlerts;

    @Column(nullable = false)
    private Boolean notifyPeriodicReports;

    @Column(nullable = false)
    private Boolean autoBackupEnabled;

    @Column(nullable = false, length = 20)
    private String backupFrequency;

    @Column(nullable = false)
    private Integer backupRetention;

    @Column(length = 150)
    private String updatedBy;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSystemDescription() {
        return systemDescription;
    }

    public void setSystemDescription(String systemDescription) {
        this.systemDescription = systemDescription;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(Integer recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public Boolean getShowAvatar() {
        return showAvatar;
    }

    public void setShowAvatar(Boolean showAvatar) {
        this.showAvatar = showAvatar;
    }

    public Boolean getCompactMode() {
        return compactMode;
    }

    public void setCompactMode(Boolean compactMode) {
        this.compactMode = compactMode;
    }

    public Integer getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(Integer passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public Boolean getRequireUppercase() {
        return requireUppercase;
    }

    public void setRequireUppercase(Boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }

    public Boolean getRequireNumber() {
        return requireNumber;
    }

    public void setRequireNumber(Boolean requireNumber) {
        this.requireNumber = requireNumber;
    }

    public Boolean getRequireSpecialChar() {
        return requireSpecialChar;
    }

    public void setRequireSpecialChar(Boolean requireSpecialChar) {
        this.requireSpecialChar = requireSpecialChar;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Integer getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(Integer maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }

    public Boolean getRequire2fa() {
        return require2fa;
    }

    public void setRequire2fa(Boolean require2fa) {
        this.require2fa = require2fa;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    public String getSmtpPass() {
        return smtpPass;
    }

    public void setSmtpPass(String smtpPass) {
        this.smtpPass = smtpPass;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public Boolean getNotifyNewLogin() {
        return notifyNewLogin;
    }

    public void setNotifyNewLogin(Boolean notifyNewLogin) {
        this.notifyNewLogin = notifyNewLogin;
    }

    public Boolean getNotifyPendingContent() {
        return notifyPendingContent;
    }

    public void setNotifyPendingContent(Boolean notifyPendingContent) {
        this.notifyPendingContent = notifyPendingContent;
    }

    public Boolean getNotifySecurityAlerts() {
        return notifySecurityAlerts;
    }

    public void setNotifySecurityAlerts(Boolean notifySecurityAlerts) {
        this.notifySecurityAlerts = notifySecurityAlerts;
    }

    public Boolean getNotifyPeriodicReports() {
        return notifyPeriodicReports;
    }

    public void setNotifyPeriodicReports(Boolean notifyPeriodicReports) {
        this.notifyPeriodicReports = notifyPeriodicReports;
    }

    public Boolean getAutoBackupEnabled() {
        return autoBackupEnabled;
    }

    public void setAutoBackupEnabled(Boolean autoBackupEnabled) {
        this.autoBackupEnabled = autoBackupEnabled;
    }

    public String getBackupFrequency() {
        return backupFrequency;
    }

    public void setBackupFrequency(String backupFrequency) {
        this.backupFrequency = backupFrequency;
    }

    public Integer getBackupRetention() {
        return backupRetention;
    }

    public void setBackupRetention(Integer backupRetention) {
        this.backupRetention = backupRetention;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
