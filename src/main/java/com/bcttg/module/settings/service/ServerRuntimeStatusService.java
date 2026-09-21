package com.bcttg.module.settings.service;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;

import com.bcttg.module.dashboard.entity.SystemAuditLog;
import com.bcttg.module.dashboard.repository.SystemAuditLogRepository;
import com.bcttg.module.dashboard.service.AuditLogMessageService;
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
    private final AuditLogMessageService auditLogMessageService;

    public ServerRuntimeStatusService(
        DataSource dataSource,
        SystemAuditLogRepository auditLogRepository,
        UserAccountRepository userAccountRepository,
        SecurityProperties securityProperties,
        MediaProperties mediaProperties,
        SettingsOperationsProperties operationsProperties,
        SystemSettingsRepository settingsRepository,
        AuditLogMessageService auditLogMessageService
    ) {
        this.dataSource = dataSource;
        this.auditLogRepository = auditLogRepository;
        this.userAccountRepository = userAccountRepository;
        this.securityProperties = securityProperties;
        this.mediaProperties = mediaProperties;
        this.operationsProperties = operationsProperties;
        this.settingsRepository = settingsRepository;
        this.auditLogMessageService = auditLogMessageService;
    }

    public List<SystemStatusCardResponse> getStatusCards() {
        SystemSettings settings = settingsRepository.findTopByDeletedAtIsNullOrderByIdAsc().orElse(null);
        return List.of(
            buildDatabaseStatus(),
            buildActiveAccountStatus(),
            buildActivityStatus(),
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
                return new SystemStatusCardResponse("database", "CSDL", formatMegabytes(schemaSize) + "/" + formatMegabytes(total), state);
            }
            return new SystemStatusCardResponse("database", "CSDL", formatMegabytes(schemaSize) + "/" + formatMegabytes(schemaSize), "GOOD");
        } catch (Exception ex) {
            return new SystemStatusCardResponse("database", "CSDL", "Không đọc được dữ liệu", "ERROR");
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
        long activeAccounts = auditLogRepository.countDistinctSuccessfulActorsSince(Instant.now().minus(30L, ChronoUnit.DAYS));
        if (activeAccounts > totalAccounts) {
            activeAccounts = totalAccounts;
        }
        String state = activeAccounts == 0L ? "WARN" : "GOOD";
        return new SystemStatusCardResponse("active_accounts", "Tài khoản hoạt động", activeAccounts + "/" + totalAccounts, state);
    }

    private SystemStatusCardResponse buildActivityStatus() {
        Optional<SystemAuditLog> latestActivity = auditLogRepository.findTopByDeletedAtIsNullOrderByCreatedAtDesc();
        if (latestActivity.isEmpty()) {
            return new SystemStatusCardResponse("system_logs", "Nhật ký hoạt động", "Chưa có dữ liệu thực tế", "WARN");
        }
        SystemAuditLog log = latestActivity.get();
        String state = "FAILED".equalsIgnoreCase(log.getStatus()) ? "WARN" : "GOOD";
        return new SystemStatusCardResponse("system_logs", "Nhật ký hoạt động", auditLogMessageService.buildMessage(log), state);
    }

    private SystemStatusCardResponse buildSecurityStatus(SystemSettings settings) {
        List<String> risks = new ArrayList<>();
        String jwtSecret = securityProperties.getJwt().getSecret();
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.contains("ChangeMe") || jwtSecret.length() < 32) {
            risks.add("JWT yếu");
        }
        if (settings != null) {
            if (!Boolean.TRUE.equals(settings.getRequire2fa())) {
                risks.add("2FA đang tắt");
            }
            if (settings.getPasswordMinLength() != null && settings.getPasswordMinLength() < 8) {
                risks.add("Độ dài mật khẩu thấp");
            }
            if (!Boolean.TRUE.equals(settings.getRequireUppercase()) || !Boolean.TRUE.equals(settings.getRequireNumber())) {
                risks.add("Chính sách mật khẩu chưa đủ mạnh");
            }
            if (settings.getSessionTimeout() != null && settings.getSessionTimeout() > 240) {
                risks.add("Phiên đăng nhập quá dài");
            }
        }
        if (risks.isEmpty()) {
            return new SystemStatusCardResponse("security", "Bảo mật", "JWT: an toàn", "GOOD");
        }
        String state = risks.contains("JWT yếu") ? "ERROR" : "WARN";
        return new SystemStatusCardResponse("security", "Bảo mật", String.join(" • ", risks), state);
    }

    private SystemStatusCardResponse buildSslStatus() {
        String targetUrl = operationsProperties.getSslCheckUrl();
        if (targetUrl == null || targetUrl.isBlank()) {
            targetUrl = mediaProperties.getBaseUrl();
        }
        if (targetUrl == null || targetUrl.isBlank()) {
            return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "Chưa cấu hình địa chỉ kiểm tra", "WARN");
        }
        try {
            URI uri = URI.create(targetUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "Không sử dụng HTTPS", "WARN");
            }
            HttpsURLConnection connection = (HttpsURLConnection) uri.toURL().openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            X509Certificate certificate = (X509Certificate) connection.getServerCertificates()[0];
            Instant notAfter = certificate.getNotAfter().toInstant();
            long remainingDays = Duration.between(Instant.now(), notAfter).toDays();
            if (remainingDays < 0L) {
                return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "Đã hết hạn", "ERROR");
            }
            String state = remainingDays < 15L ? "ERROR" : remainingDays < 30L ? "WARN" : "GOOD";
            return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", remainingDays + " ngày", state);
        } catch (Exception ex) {
            return new SystemStatusCardResponse("ssl_tls", "SSL/TLS", "Không kiểm tra được chứng chỉ", "WARN");
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

    private String formatMegabytes(long bytes) {
        double megabytes = bytes / 1024D / 1024D;
        if (Math.abs(megabytes - Math.rint(megabytes)) < 0.05D) {
            return String.valueOf((long) Math.rint(megabytes)) + " MB";
        }
        return String.format(java.util.Locale.US, "%.1f MB", megabytes);
    }
}
