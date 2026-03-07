package com.bcttg.module.settings;

public interface SystemSettingsWritable {
    String getSystemName();

    String getSystemDescription();

    String getTimezone();

    String getLanguage();

    Integer getRecordsPerPage();

    Boolean getShowAvatar();

    Boolean getCompactMode();

    Integer getPasswordMinLength();

    Boolean getRequireUppercase();

    Boolean getRequireNumber();

    Boolean getRequireSpecialChar();

    Integer getSessionTimeout();

    Integer getMaxLoginAttempts();

    Boolean getRequire2fa();

    String getSmtpHost();

    Integer getSmtpPort();

    String getSmtpUser();

    String getSmtpPass();

    String getEmailFrom();

    Boolean getNotifyNewLogin();

    Boolean getNotifyPendingContent();

    Boolean getNotifySecurityAlerts();

    Boolean getNotifyPeriodicReports();

    Boolean getAutoBackupEnabled();

    String getBackupFrequency();

    Integer getBackupRetention();
}
