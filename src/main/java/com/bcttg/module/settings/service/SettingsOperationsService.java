package com.bcttg.module.settings.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.sql.DataSource;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.dashboard.service.SystemAuditTrailService;
import com.bcttg.module.settings.SettingsOperationsProperties;
import com.bcttg.module.settings.dto.BackupFileResponse;
import com.bcttg.module.settings.dto.CacheClearResponse;
import com.bcttg.module.settings.dto.OperationMessageResponse;
import com.bcttg.module.settings.dto.TestEmailRequest;
import com.bcttg.module.settings.dto.TestEmailResponse;
import com.bcttg.module.settings.entity.SystemSettings;
import com.bcttg.module.settings.repository.SystemSettingsRepository;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsOperationsService {
    private static final long RESTORE_TIMEOUT_MINUTES = 10L;

    private final SettingsOperationsProperties operationsProperties;
    private final SystemSettingsRepository settingsRepository;
    private final ObjectProvider<CacheManager> cacheManagerProvider;
    private final DataSource dataSource;
    private final SystemAuditTrailService auditTrailService;
    private final Environment environment;

    public SettingsOperationsService(
        SettingsOperationsProperties operationsProperties,
        SystemSettingsRepository settingsRepository,
        ObjectProvider<CacheManager> cacheManagerProvider,
        DataSource dataSource,
        SystemAuditTrailService auditTrailService,
        Environment environment
    ) {
        this.operationsProperties = operationsProperties;
        this.settingsRepository = settingsRepository;
        this.cacheManagerProvider = cacheManagerProvider;
        this.dataSource = dataSource;
        this.auditTrailService = auditTrailService;
        this.environment = environment;
    }

    @Transactional(readOnly = true)
    public TestEmailResponse testEmail(TestEmailRequest request, String actorPhone) {
        SystemSettings settings = settingsRepository.findTopByDeletedAtIsNullOrderByIdAsc()
            .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Chua co cau hinh SMTP"));
        if (settings.getSmtpHost() == null || settings.getSmtpHost().isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "SMTP host chua duoc cau hinh");
        }
        if (settings.getEmailFrom() == null || settings.getEmailFrom().isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Email gui di chua duoc cau hinh");
        }
        try {
            Session session = createMailSession(settings);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(settings.getEmailFrom()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getTo()));
            message.setSubject(
                request.getSubject() == null || request.getSubject().isBlank()
                    ? "BCTTG - Kiem tra cau hinh email"
                    : request.getSubject(),
                "UTF-8"
            );
            String body = request.getBody() == null || request.getBody().isBlank()
                ? "He thong BCTTG da gui email kiem tra thanh cong luc " + Instant.now()
                : request.getBody();
            message.setText(body, "UTF-8");
            Transport.send(message);
            auditTrailService.record(actorPhone, "TEST_EMAIL", "SETTINGS", request.getTo(), "Gui email kiem tra");
            return new TestEmailResponse(request.getTo(), Instant.now(), "Gui email kiem tra thanh cong");
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Gui email that bai", List.of(ex.getMessage()));
        }
    }

    @Transactional(readOnly = true)
    public List<BackupFileResponse> listBackups() {
        Path root = getBackupRoot();
        if (!Files.exists(root)) {
            return List.of();
        }
        try {
            return Files.walk(root)
                .filter(Files::isRegularFile)
                .sorted(Comparator.comparing(this::lastModifiedTime).reversed())
                .map(this::toBackupResponse)
                .toList();
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Khong doc duoc danh sach backup", List.of(ex.getMessage()));
        }
    }

    @Transactional
    public OperationMessageResponse restoreBackup(String id, String actorPhone) {
        Path backupFile = resolveBackupFile(id);
        if (!isSqlBackup(backupFile)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Chi ho tro phuc hoi tep .sql hoac .sql.gz");
        }
        DatabaseTarget target = resolveDatabaseTarget();
        ProcessBuilder processBuilder = new ProcessBuilder(
            operationsProperties.getMysqlClientCommand(),
            "--host=" + target.host(),
            "--port=" + target.port(),
            "--user=" + target.username(),
            "--password=" + target.password(),
            target.database()
        );
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            try (OutputStream outputStream = process.getOutputStream()) {
                if (backupFile.getFileName().toString().endsWith(".gz")) {
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(backupFile))) {
                        gzipInputStream.transferTo(outputStream);
                    }
                } else {
                    Files.copy(backupFile, outputStream);
                }
            }
            boolean finished = process.waitFor(RESTORE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            String processOutput = readProcessOutput(process);
            if (!finished) {
                process.destroyForcibly();
                throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Phuc hoi backup bi timeout");
            }
            if (process.exitValue() != 0) {
                throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Phuc hoi backup that bai", List.of(processOutput));
            }
            auditTrailService.record(actorPhone, "RESTORE", "SETTINGS", backupFile.getFileName().toString(), "Phuc hoi backup");
            return new OperationMessageResponse("Phuc hoi backup thanh cong", Instant.now());
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Khong tim thay cong cu mysql client", List.of(ex.getMessage()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Phuc hoi backup bi gian doan");
        }
    }

    @Transactional(readOnly = true)
    public FileSystemResource downloadBackup(String id) {
        return new FileSystemResource(resolveBackupFile(id));
    }

    @Transactional
    public CacheClearResponse clearCache(String actorPhone) {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager == null) {
            auditTrailService.record(actorPhone, "CLEAR_CACHE", "SETTINGS", "cache", "Khong co cache de xoa");
            return new CacheClearResponse(0, Instant.now(), "He thong chua cau hinh cache manager");
        }
        int cleared = 0;
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                cleared++;
            }
        }
        auditTrailService.record(actorPhone, "CLEAR_CACHE", "SETTINGS", "cache", "Da xoa " + cleared + " cache");
        return new CacheClearResponse(cleared, Instant.now(), "Da xoa cache thanh cong");
    }

    private Session createMailSession(SystemSettings settings) {
        java.util.Properties properties = new java.util.Properties();
        properties.put("mail.smtp.host", settings.getSmtpHost());
        properties.put("mail.smtp.port", String.valueOf(settings.getSmtpPort()));
        properties.put("mail.smtp.auth", String.valueOf(settings.getSmtpUser() != null && !settings.getSmtpUser().isBlank()));
        properties.put("mail.smtp.starttls.enable", "true");
        if (settings.getSmtpUser() == null || settings.getSmtpUser().isBlank()) {
            return Session.getInstance(properties);
        }
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(settings.getSmtpUser(), Optional.ofNullable(settings.getSmtpPass()).orElse(""));
            }
        });
    }

    private BackupFileResponse toBackupResponse(Path file) {
        try {
            return new BackupFileResponse(
                encodeFileId(file),
                getBackupRoot().relativize(file).toString().replace('\\', '/'),
                Files.size(file),
                Files.getLastModifiedTime(file).toInstant(),
                isSqlBackup(file)
            );
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Khong doc duoc thong tin backup", List.of(ex.getMessage()));
        }
    }

    private Instant lastModifiedTime(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException ex) {
            return Instant.EPOCH;
        }
    }

    private Path getBackupRoot() {
        return Paths.get(operationsProperties.getBackupRoot() == null ? "./backups" : operationsProperties.getBackupRoot()).toAbsolutePath().normalize();
    }

    private Path resolveBackupFile(String id) {
        try {
            String relativePath = new String(Base64.getUrlDecoder().decode(id));
            Path root = getBackupRoot();
            Path target = root.resolve(relativePath).normalize();
            if (!target.startsWith(root) || !Files.exists(target) || !Files.isRegularFile(target)) {
                throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Backup not found");
            }
            return target;
        } catch (IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Backup id khong hop le");
        }
    }

    private String encodeFileId(Path file) {
        String relativePath = getBackupRoot().relativize(file).toString().replace('\\', '/');
        return Base64.getUrlEncoder().withoutPadding().encodeToString(relativePath.getBytes());
    }

    private boolean isSqlBackup(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".sql") || fileName.endsWith(".sql.gz");
    }

    private DatabaseTarget resolveDatabaseTarget() {
        try (Connection connection = dataSource.getConnection()) {
            String jdbcUrl = connection.getMetaData().getURL();
            String withoutPrefix = jdbcUrl.replace("jdbc:mysql://", "");
            String hostPart = withoutPrefix;
            int queryIndex = hostPart.indexOf('?');
            if (queryIndex >= 0) {
                hostPart = hostPart.substring(0, queryIndex);
            }
            int slashIndex = hostPart.indexOf('/');
            String hostAndPort = slashIndex >= 0 ? hostPart.substring(0, slashIndex) : hostPart;
            String database = slashIndex >= 0 ? hostPart.substring(slashIndex + 1) : "";
            int colonIndex = hostAndPort.indexOf(':');
            String host = colonIndex >= 0 ? hostAndPort.substring(0, colonIndex) : hostAndPort;
            String port = colonIndex >= 0 ? hostAndPort.substring(colonIndex + 1) : "3306";
            String username = Optional.ofNullable(environment.getProperty("spring.datasource.username"))
                .orElse(connection.getMetaData().getUserName());
            String password = Optional.ofNullable(environment.getProperty("spring.datasource.password"))
                .orElse("");
            if (password.isBlank()) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Khong lay duoc mat khau ket noi CSDL de phuc hoi backup");
            }
            return new DatabaseTarget(host, port, database, username, password);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Khong doc duoc cau hinh ket noi CSDL", List.of(ex.getMessage()));
        }
    }

    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputStream.write(line.getBytes());
                outputStream.write('\n');
            }
            return outputStream.toString();
        }
    }

    private record DatabaseTarget(String host, String port, String database, String username, String password) {
    }
}
