package com.bcttg.module.settings.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

import javax.sql.DataSource;

import com.bcttg.module.dashboard.entity.SystemAuditLog;
import com.bcttg.module.dashboard.repository.SystemAuditLogRepository;
import com.bcttg.module.media.MediaProperties;
import com.bcttg.module.settings.SettingsOperationsProperties;
import com.bcttg.module.settings.dto.SystemStatusCardResponse;
import com.bcttg.module.settings.entity.SystemSettings;
import com.bcttg.module.settings.repository.SystemSettingsRepository;
import com.bcttg.module.user.repository.UserAccountRepository;
import com.bcttg.security.SecurityProperties;

import org.springframework.stereotype.Service;

@Service
public class ServerRuntimeStatusService {
    private final DataSource dataSource;
    private final SystemAuditLogRepository auditLogRepository;
    private final UserAccountRepository userAccountRepository;
    private final SecurityProperties securityProperties;
    private final MediaProperties mediaProperties;
    private final SettingsOperationsProperties operationsProperties;
    private final SystemSettingsRepository settingsRepository;

    public ServerRuntimeStatusService(
        DataSource dataSource,
        SystemAuditLogRepository auditLogRepository,
        UserAccountRepository userAccountRepository,
        SecurityProperties securityProperties,
        MediaProperties mediaProperties,
        SettingsOperationsProperties operationsProperties,
        SystemSettingsRepository settingsRepository
    ) {
        this.dataSource = dataSource;
        this.auditLogRepository = auditLogRepository;
        this.userAccountRepository = userAccountRepository;
        this.securityProperties = securityProperties;
        this.mediaProperties = mediaProperties;
        this.operationsProperties = operationsProperties;
        this.settingsRepository = settingsRepository;
    }

    public List<SystemStatusCardResponse> getStatusCards() {
        SystemSettings settings = settingsRepository.findTopByDeletedAtIsNullOrderByIdAsc().orElse(null);
        return List.of(
            buildDatabaseStatus(),
            buildActiveAccountStatus(),
            buildLoginStatus(),
            buildSecurityStatus(settings),
            buildSslStatus()
        );
    }

    private SystemStatusCardResponse buildDatabaseStatus() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            long schemaSize = queryLong(statement, "SELECT COALESCE(SUM(data_length + index_length), 0) FROM information_schema.TABLES WHERE table_schema = DATABASE()");
            String datadir = queryString(statement, "SELECT @@datadir");
            Path dataPath = resolveDatabasePath(datadir);
            if (dataPath != null && Files.exists(dataPath)) {
                long total = Files.getFileStore(dataPath).getTotalSpace();
                long usable = Files.getFileStore(dataPath).getUsableSpace();
                double freeRatio = total > 0L ? (double) usable / (double) total : 0D;
                String state = freeRatio < 0.1D ? "ERROR" : freeRatio < 0.2D ? "WARN" : "GOOD";
                return new SystemStatusCardResponse(
                    "database",
                    "Co so du lieu",
                    formatSize(usable) + " trong / " + formatSize(total) + " • schema da dung " + formatSize(schemaSize),
                    state
                );
            }
            return new SystemStatusCardResponse(
                "database",
                "Co so du lieu",
                "Schema da dung " + formatSize(schemaSize),
                "GOOD"
            );
        } catch (Exception ex) {
            return new SystemStatusCardResponse("database", "Co so du lieu", "Khong doc duoc thong tin dung luong", "ERROR");
        }
    }

    private Path resolveDatabasePath(String datadir) {
        if (datadir != null && !datadir.isBlank()) {
            Path directPath = Paths.get(datadir).normalize();
            if (Files.exists(directPath)) {
                return directPath;
            }
        }
        String fallbackRoot = operationsProperties.getDatabaseStorageRoot();
        if (fallbackRoot == null || fallbackRoot.isBlank()) {
            return null;
        }
        Path fallbackPath = Paths.get(fallbackRoot).normalize();
        return Files.exists(fallbackPath) ? fallbackPath : null;
    }

    private SystemStatusCardResponse buildActiveAccountStatus() {
        long totalAccounts = userAccountRepository.countByDeletedAtIsNull();
        long activeAccounts = userAccountRepository.countByDeletedAtIsNullAndIsActiveTrue();
        String state = activeAccounts == 0L ? "WARN" : "GOOD";
        return new SystemStatusCardResponse(
            "active_accounts",
            "Tai khoan hoat dong",
            activeAccounts + " / " + totalAccounts + " tai khoan dang hoat dong",
            state
        );
    }

    private SystemStatusCardResponse buildLoginStatus() {
        Optional<SystemAuditLog> latestLogin = auditLogRepository.findTopByDeletedAtIsNullAndActionTypeOrderByCreatedAtDesc("LOGIN");
        if (latestLogin.isEmpty()) {
            return new SystemStatusCardResponse("system_logs", "Nhat ky he thong", "Chua co du lieu dang nhap thuc te", "WARN");
        }
        SystemAuditLog log = latestLogin.get();
        String actorName = log.getActorName() == null || log.getActorName().isBlank() ? log.getEntityName() : log.getActorName();
        String value = actorName + " • " + ("FAILED".equalsIgnoreCase(log.getStatus()) ? "that bai" : "thanh cong") + " • " + formatRelative(log.getCreatedAt());
        String state = "FAILED".equalsIgnoreCase(log.getStatus()) ? "WARN" : "GOOD";
        return new SystemStatusCardResponse("system_logs", "Nhat ky he thong", value, state);
    }

    private SystemStatusCardResponse buildSecurityStatus(SystemSettings settings) {
        List<String> risks = new ArrayList<>();
        String jwtSecret = securityProperties.getJwt().getSecret();
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.contains("ChangeMe") || jwtSecret.length() < 32) {
            risks.add("JWT secret yeu");
        }
        if (settings != null) {
            if (!Boolean.TRUE.equals(settings.getRequire2fa())) {
                risks.add("2FA dang tat");
            }
            if (settings.getPasswordMinLength() != null && settings.getPasswordMinLength() < 8) {
                risks.add("Do dai mat khau thap");
            }
            if (!Boolean.TRUE.equals(settings.getRequireUppercase()) || !Boolean.TRUE.equals(settings.getRequireNumber())) {
                risks.add("Chinh sach mat khau chua du manh");
            }
            if (settings.getSessionTimeout() != null && settings.getSessionTimeout() > 240) {
                risks.add("Session timeout qua dai");
            }
        }
        if (risks.isEmpty()) {
            return new SystemStatusCardResponse("security", "Bao mat", "Khong phat hien nguy co cao", "GOOD");
        }
        String state = risks.contains("JWT secret yeu") ? "ERROR" : "WARN";
        return new SystemStatusCardResponse("security", "Bao mat", risks.size() + " nguy co: " + String.join(", ", risks), state);
    }

    private SystemStatusCardResponse buildSslStatus() {
        String targetUrl = operationsProperties.getSslCheckUrl();
        if (targetUrl == null || targetUrl.isBlank()) {
            targetUrl = mediaProperties.getBaseUrl();
        }
        if (targetUrl == null || targetUrl.isBlank()) {
            return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "Chua cau hinh dia chi kiem tra", "WARN");
        }
        try {
            URI uri = URI.create(targetUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "He thong khong su dung HTTPS", "WARN");
            }
            HttpsURLConnection connection = (HttpsURLConnection) uri.toURL().openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            X509Certificate certificate = (X509Certificate) connection.getServerCertificates()[0];
            Instant notAfter = certificate.getNotAfter().toInstant();
            long remainingDays = Duration.between(Instant.now(), notAfter).toDays();
            if (remainingDays < 0L) {
                return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "Chung chi da het han", "ERROR");
            }
            String state = remainingDays < 15L ? "ERROR" : remainingDays < 30L ? "WARN" : "GOOD";
            return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "Con " + remainingDays + " ngay", state);
        } catch (Exception ex) {
            return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "Khong kiem tra duoc chung chi", "WARN");
        }
    }

    private long queryLong(Statement statement, String sql) throws Exception {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                return 0L;
            }
            return resultSet.getLong(1);
        }
    }

    private String queryString(Statement statement, String sql) throws Exception {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                return null;
            }
            return resultSet.getString(1);
        }
    }

    private String formatRelative(Instant instant) {
        long minutes = Math.max(0L, Duration.between(instant, Instant.now()).toMinutes());
        if (minutes < 1L) {
            return "vua xong";
        }
        if (minutes < 60L) {
            return minutes + " phut truoc";
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + " gio truoc";
        }
        long days = hours / 24L;
        return days + " ngay truoc";
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
}
