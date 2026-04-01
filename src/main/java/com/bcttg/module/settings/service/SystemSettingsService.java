package com.bcttg.module.settings.service;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.bcttg.module.dashboard.service.SystemAuditTrailService;
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
    private final SystemAuditTrailService auditTrailService;
    private final Environment environment;
    private final ServerRuntimeStatusService runtimeStatusService;

    public SystemSettingsService(
        SystemSettingsRepository repository,
        SystemSettingsDefaultsProperties defaults,
        SystemAuditTrailService auditTrailService,
        Environment environment,
        ServerRuntimeStatusService runtimeStatusService
    ) {
        this.repository = repository;
        this.defaults = defaults;
        this.auditTrailService = auditTrailService;
        this.environment = environment;
        this.runtimeStatusService = runtimeStatusService;
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
        SystemSettings settings = repository.findTopByDeletedAtIsNullOrderByIdAsc()
            .orElseGet(() -> createSettingsFromWritable(defaults));
        mergeWritable(settings, request);
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
        return runtimeStatusService.getStatusCards();
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

    private void mergeWritable(SystemSettings settings, SystemSettingsRequest request) {
        if (request.getSystemName() != null) {
            settings.setSystemName(request.getSystemName());
        }
        if (request.getSystemDescription() != null) {
            settings.setSystemDescription(request.getSystemDescription());
        }
        if (request.getTimezone() != null) {
            settings.setTimezone(request.getTimezone());
        }
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        if (request.getRecordsPerPage() != null) {
            settings.setRecordsPerPage(request.getRecordsPerPage());
        }
        if (request.getShowAvatar() != null) {
            settings.setShowAvatar(request.getShowAvatar());
        }
        if (request.getCompactMode() != null) {
            settings.setCompactMode(request.getCompactMode());
        }
        if (request.getPasswordMinLength() != null) {
            settings.setPasswordMinLength(request.getPasswordMinLength());
        }
        if (request.getRequireUppercase() != null) {
            settings.setRequireUppercase(request.getRequireUppercase());
        }
        if (request.getRequireNumber() != null) {
            settings.setRequireNumber(request.getRequireNumber());
        }
        if (request.getRequireSpecialChar() != null) {
            settings.setRequireSpecialChar(request.getRequireSpecialChar());
        }
        if (request.getSessionTimeout() != null) {
            settings.setSessionTimeout(request.getSessionTimeout());
        }
        if (request.getMaxLoginAttempts() != null) {
            settings.setMaxLoginAttempts(request.getMaxLoginAttempts());
        }
        if (request.getRequire2fa() != null) {
            settings.setRequire2fa(request.getRequire2fa());
        }
        if (request.getSmtpHost() != null) {
            settings.setSmtpHost(blankToNull(request.getSmtpHost()));
        }
        if (request.getSmtpPort() != null) {
            settings.setSmtpPort(request.getSmtpPort());
        }
        if (request.getSmtpUser() != null) {
            settings.setSmtpUser(blankToNull(request.getSmtpUser()));
        }
        if (request.getSmtpPass() != null && !request.getSmtpPass().isBlank()) {
            settings.setSmtpPass(request.getSmtpPass());
        }
        if (request.getEmailFrom() != null) {
            settings.setEmailFrom(blankToNull(request.getEmailFrom()));
        }
        if (request.getNotifyNewLogin() != null) {
            settings.setNotifyNewLogin(request.getNotifyNewLogin());
        }
        if (request.getNotifyPendingContent() != null) {
            settings.setNotifyPendingContent(request.getNotifyPendingContent());
        }
        if (request.getNotifySecurityAlerts() != null) {
            settings.setNotifySecurityAlerts(request.getNotifySecurityAlerts());
        }
        if (request.getNotifyPeriodicReports() != null) {
            settings.setNotifyPeriodicReports(request.getNotifyPeriodicReports());
        }
        if (request.getAutoBackupEnabled() != null) {
            settings.setAutoBackupEnabled(request.getAutoBackupEnabled());
        }
        if (request.getBackupFrequency() != null) {
            settings.setBackupFrequency(request.getBackupFrequency());
        }
        if (request.getBackupRetention() != null) {
            settings.setBackupRetention(request.getBackupRetention());
        }
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

}
