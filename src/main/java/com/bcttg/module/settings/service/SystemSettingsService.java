package com.bcttg.module.settings.service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.bcttg.module.dashboard.service.SystemAuditTrailService;
import com.bcttg.module.media.MediaProperties;
import com.bcttg.module.settings.SystemSettingsDefaultsProperties;
import com.bcttg.module.settings.SystemSettingsWritable;
import com.bcttg.module.settings.dto.SystemSettingsRequest;
import com.bcttg.module.settings.dto.SystemSettingsResponse;
import com.bcttg.module.settings.dto.SystemStatusCardResponse;
import com.bcttg.module.settings.dto.SystemVersionResponse;
import com.bcttg.module.settings.entity.SystemSettings;
import com.bcttg.module.settings.repository.SystemSettingsRepository;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemSettingsService {
    private final SystemSettingsRepository repository;
    private final SystemSettingsDefaultsProperties defaults;
    private final MediaProperties mediaProperties;
    private final SystemAuditTrailService auditTrailService;
    private final DataSource dataSource;
    private final Environment environment;

    public SystemSettingsService(
        SystemSettingsRepository repository,
        SystemSettingsDefaultsProperties defaults,
        MediaProperties mediaProperties,
        SystemAuditTrailService auditTrailService,
        DataSource dataSource,
        Environment environment
    ) {
        this.repository = repository;
        this.defaults = defaults;
        this.mediaProperties = mediaProperties;
        this.auditTrailService = auditTrailService;
        this.dataSource = dataSource;
        this.environment = environment;
    }

    @Transactional(readOnly = true)
    public SystemSettingsResponse getSettings() {
        Optional<SystemSettings> settings = repository.findTopByDeletedAtIsNullOrderByIdAsc();
        if (settings.isPresent()) {
            return toResponse(settings.get());
        }
        return toResponse(createSettingsFromWritable(defaults));
    }

    @Transactional
    public SystemSettingsResponse update(SystemSettingsRequest request, String actorPhone) {
        SystemSettings settings = repository.findTopByDeletedAtIsNullOrderByIdAsc().orElseGet(SystemSettings::new);
        String smtpPass = resolveSmtpPass(settings.getSmtpPass(), request.getSmtpPass());
        applyWritable(settings, request, smtpPass);
        settings.setUpdatedBy(actorPhone);
        SystemSettings saved = repository.save(settings);
        auditTrailService.record(actorPhone, "UPDATE", "SETTINGS", "system_settings", "Cap nhat cau hinh he thong");
        return toResponse(saved);
    }

    @Transactional
    public SystemSettingsResponse reset(String actorPhone) {
        SystemSettings settings = repository.findTopByDeletedAtIsNullOrderByIdAsc().orElseGet(SystemSettings::new);
        applyWritable(settings, defaults, defaults.getSmtpPass());
        settings.setUpdatedBy(actorPhone);
        SystemSettings saved = repository.save(settings);
        auditTrailService.record(actorPhone, "UPDATE", "SETTINGS", "system_settings", "Khoi phuc cau hinh mac dinh");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SystemStatusCardResponse> getStatusCards() {
        Optional<SystemSettings> settings = repository.findTopByDeletedAtIsNullOrderByIdAsc();
        return List.of(
            buildDatabaseStatus(),
            buildStorageStatus(),
            buildUptimeStatus(),
            buildSettingsStatus(settings.orElse(null))
        );
    }

    @Transactional(readOnly = true)
    public SystemVersionResponse getVersion() {
        String packageVersion = Optional.ofNullable(SystemSettingsService.class.getPackage().getImplementationVersion())
            .orElse("0.0.1-SNAPSHOT");
        Instant startedAt = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
        return new SystemVersionResponse(
            environment.getProperty("spring.application.name", "bcttg"),
            packageVersion,
            Arrays.asList(environment.getActiveProfiles()),
            System.getProperty("java.version"),
            startedAt
        );
    }

    private SystemSettingsResponse toResponse(SystemSettings settings) {
        String smtpPass = settings.getSmtpPass();
        return new SystemSettingsResponse(
            settings.getSystemName(),
            settings.getSystemDescription(),
            settings.getTimezone(),
            settings.getLanguage(),
            settings.getRecordsPerPage(),
            settings.getShowAvatar(),
            settings.getCompactMode(),
            settings.getPasswordMinLength(),
            settings.getRequireUppercase(),
            settings.getRequireNumber(),
            settings.getRequireSpecialChar(),
            settings.getSessionTimeout(),
            settings.getMaxLoginAttempts(),
            settings.getRequire2fa(),
            settings.getSmtpHost(),
            settings.getSmtpPort(),
            settings.getSmtpUser(),
            smtpPass != null && !smtpPass.isBlank(),
            mask(smtpPass),
            settings.getEmailFrom(),
            settings.getNotifyNewLogin(),
            settings.getNotifyPendingContent(),
            settings.getNotifySecurityAlerts(),
            settings.getNotifyPeriodicReports(),
            settings.getAutoBackupEnabled(),
            settings.getBackupFrequency(),
            settings.getBackupRetention(),
            getVersion().getVersion(),
            settings.getUpdatedAt(),
            settings.getUpdatedBy()
        );
    }

    private SystemSettings createSettingsFromWritable(SystemSettingsWritable writable) {
        SystemSettings settings = new SystemSettings();
        applyWritable(settings, writable, writable.getSmtpPass());
        return settings;
    }

    private void applyWritable(SystemSettings settings, SystemSettingsWritable writable, String smtpPass) {
        settings.setSystemName(writable.getSystemName());
        settings.setSystemDescription(writable.getSystemDescription());
        settings.setTimezone(writable.getTimezone());
        settings.setLanguage(writable.getLanguage());
        settings.setRecordsPerPage(writable.getRecordsPerPage());
        settings.setShowAvatar(writable.getShowAvatar());
        settings.setCompactMode(writable.getCompactMode());
        settings.setPasswordMinLength(writable.getPasswordMinLength());
        settings.setRequireUppercase(writable.getRequireUppercase());
        settings.setRequireNumber(writable.getRequireNumber());
        settings.setRequireSpecialChar(writable.getRequireSpecialChar());
        settings.setSessionTimeout(writable.getSessionTimeout());
        settings.setMaxLoginAttempts(writable.getMaxLoginAttempts());
        settings.setRequire2fa(writable.getRequire2fa());
        settings.setSmtpHost(blankToNull(writable.getSmtpHost()));
        settings.setSmtpPort(writable.getSmtpPort());
        settings.setSmtpUser(blankToNull(writable.getSmtpUser()));
        settings.setSmtpPass(blankToNull(smtpPass));
        settings.setEmailFrom(blankToNull(writable.getEmailFrom()));
        settings.setNotifyNewLogin(writable.getNotifyNewLogin());
        settings.setNotifyPendingContent(writable.getNotifyPendingContent());
        settings.setNotifySecurityAlerts(writable.getNotifySecurityAlerts());
        settings.setNotifyPeriodicReports(writable.getNotifyPeriodicReports());
        settings.setAutoBackupEnabled(writable.getAutoBackupEnabled());
        settings.setBackupFrequency(writable.getBackupFrequency());
        settings.setBackupRetention(writable.getBackupRetention());
    }

    private String resolveSmtpPass(String currentValue, String requestedValue) {
        if (requestedValue == null || requestedValue.isBlank()) {
            return currentValue;
        }
        return requestedValue;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "********";
    }

    private SystemStatusCardResponse buildDatabaseStatus() {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            return new SystemStatusCardResponse("database", "Co so du lieu", valid ? "Ket noi on dinh" : "Mat ket noi", valid ? "GOOD" : "ERROR");
        } catch (Exception ex) {
            return new SystemStatusCardResponse("database", "Co so du lieu", "Khong ket noi duoc", "ERROR");
        }
    }

    private SystemStatusCardResponse buildStorageStatus() {
        Path storagePath = Path.of(mediaProperties.getStorageRoot() == null ? "./storage" : mediaProperties.getStorageRoot());
        File storage = storagePath.toFile();
        if (!storage.exists()) {
            return new SystemStatusCardResponse("storage", "Luu tru media", "Thu muc chua ton tai", "WARN");
        }
        long totalSpace = storage.getTotalSpace();
        long usableSpace = storage.getUsableSpace();
        long usedSpace = totalSpace - usableSpace;
        double usedRatio = totalSpace > 0 ? (double) usedSpace / (double) totalSpace : 0D;
        String state = usedRatio >= 0.9D ? "ERROR" : usedRatio >= 0.75D ? "WARN" : "GOOD";
        return new SystemStatusCardResponse(
            "storage",
            "Luu tru media",
            formatSize(usableSpace) + " trong / " + formatSize(totalSpace),
            state
        );
    }

    private SystemStatusCardResponse buildUptimeStatus() {
        Instant startedAt = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
        Duration duration = Duration.between(startedAt, Instant.now());
        return new SystemStatusCardResponse("uptime", "Thoi gian hoat dong", formatDuration(duration), "INFO");
    }

    private SystemStatusCardResponse buildSettingsStatus(SystemSettings settings) {
        if (settings == null || settings.getUpdatedAt() == null) {
            return new SystemStatusCardResponse("settings", "Cau hinh he thong", "Dang dung gia tri mac dinh", "INFO");
        }
        String updatedBy = settings.getUpdatedBy() == null || settings.getUpdatedBy().isBlank()
            ? "System"
            : settings.getUpdatedBy();
        return new SystemStatusCardResponse("settings", "Cau hinh he thong", updatedBy + " cap nhat luc " + settings.getUpdatedAt(), "GOOD");
    }

    private String formatSize(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        }
        double kilobytes = bytes / 1024D;
        if (kilobytes < 1024D) {
            return String.format("%.1f KB", kilobytes);
        }
        double megabytes = kilobytes / 1024D;
        if (megabytes < 1024D) {
            return String.format("%.1f MB", megabytes);
        }
        double gigabytes = megabytes / 1024D;
        return String.format("%.1f GB", gigabytes);
    }

    private String formatDuration(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long days = totalMinutes / (60L * 24L);
        long hours = (totalMinutes % (60L * 24L)) / 60L;
        long minutes = totalMinutes % 60L;
        if (days > 0L) {
            return days + " ngay " + hours + " gio";
        }
        if (hours > 0L) {
            return hours + " gio " + minutes + " phut";
        }
        return minutes + " phut";
    }
}
