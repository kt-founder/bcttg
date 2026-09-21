package com.bcttg.module.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.operations")
public class SettingsOperationsProperties {
    private String backupRoot;
    private String sslCheckUrl;
    private String mysqlClientCommand;
    private String databaseStorageRoot;

    public String getBackupRoot() {
        return backupRoot;
    }

    public void setBackupRoot(String backupRoot) {
        this.backupRoot = backupRoot;
    }

    public String getSslCheckUrl() {
        return sslCheckUrl;
    }

    public void setSslCheckUrl(String sslCheckUrl) {
        this.sslCheckUrl = sslCheckUrl;
    }

    public String getMysqlClientCommand() {
        return mysqlClientCommand;
    }

    public void setMysqlClientCommand(String mysqlClientCommand) {
        this.mysqlClientCommand = mysqlClientCommand;
    }

    public String getDatabaseStorageRoot() {
        return databaseStorageRoot;
    }

    public void setDatabaseStorageRoot(String databaseStorageRoot) {
        this.databaseStorageRoot = databaseStorageRoot;
    }
}
