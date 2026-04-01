package com.bcttg.module.log.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.bcttg.common.AuditDetailUtil;
import com.bcttg.common.ExcelExportUtil;
import com.bcttg.common.TimeRange;
import com.bcttg.common.TimeRangeResolver;
import com.bcttg.module.dashboard.entity.SystemAuditLog;
import com.bcttg.module.dashboard.repository.SystemAuditLogRepository;
import com.bcttg.module.dashboard.service.AuditLogMessageService;
import com.bcttg.module.log.dto.LogSummaryResponse;
import com.bcttg.module.log.dto.LoginLogResponse;
import com.bcttg.module.log.dto.SystemLogResponse;
import com.bcttg.module.user.entity.UserProfile;
import com.bcttg.module.user.repository.UserProfileRepository;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;

@Service
public class AdminLogService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final SystemAuditLogRepository auditLogRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuditLogMessageService auditLogMessageService;

    public AdminLogService(
        SystemAuditLogRepository auditLogRepository,
        UserProfileRepository userProfileRepository,
        AuditLogMessageService auditLogMessageService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userProfileRepository = userProfileRepository;
        this.auditLogMessageService = auditLogMessageService;
    }

    @Transactional(readOnly = true)
    public Page<LoginLogResponse> getLoginLogs(
        String q,
        String status,
        String action,
        String period,
        Instant from,
        Instant to,
        Pageable pageable
    ) {
        TimeRange timeRange = TimeRangeResolver.resolve(period, from, to, ZoneId.systemDefault());
        Page<SystemAuditLog> logs = auditLogRepository.findAll(buildLoginSpecification(q, status, action, timeRange), pageable);
        Map<Long, UserProfile> profiles = loadProfiles(logs.getContent());
        return logs.map(log -> toLoginLogResponse(log, profiles));
    }

    @Transactional(readOnly = true)
    public Page<SystemLogResponse> getSystemLogs(
        String q,
        String level,
        String module,
        String action,
        String period,
        Instant from,
        Instant to,
        Pageable pageable
    ) {
        TimeRange timeRange = TimeRangeResolver.resolve(period, from, to, ZoneId.systemDefault());
        Page<SystemAuditLog> logs = auditLogRepository.findAll(buildSystemSpecification(q, level, module, action, timeRange), pageable);
        return logs.map(this::toSystemLogResponse);
    }

    @Transactional(readOnly = true)
    public LogSummaryResponse getSummary() {
        TimeRange timeRange = TimeRangeResolver.resolve("month", null, null, ZoneId.systemDefault());
        long totalLogins = auditLogRepository.count(buildLoginSpecification(null, null, null, timeRange));
        long successfulLogins = auditLogRepository.count(buildLoginSpecification(null, "SUCCESS", null, timeRange));
        long failedLogins = auditLogRepository.count(buildLoginSpecification(null, "FAILED", null, timeRange));
        long systemActions = auditLogRepository.count(buildSystemSpecification(null, null, null, null, timeRange));
        return new LogSummaryResponse(totalLogins, successfulLogins, failedLogins, systemActions);
    }

    @Transactional(readOnly = true)
    public ByteArrayResource exportLogs(
        String type,
        String format,
        String q,
        String status,
        String level,
        String module,
        String action,
        String period,
        Instant from,
        Instant to
    ) {
        if (!"xlsx".equalsIgnoreCase(format)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Chi ho tro dinh dang xlsx");
        }
        if ("login".equalsIgnoreCase(type)) {
            List<LoginLogResponse> rows = getLoginLogs(q, status, action, period, from, to, Pageable.unpaged()).getContent();
            return ExcelExportUtil.buildWorkbook(
                "login_logs",
                List.of("ID", "User ID", "Nguoi dung", "Don vi", "Hanh dong", "IP", "Thiet bi", "Trang thai", "Ly do that bai", "Thoi gian"),
                rows.stream()
                    .map(row -> List.of(
                        safe(row.getId()),
                        safe(row.getUserId()),
                        safe(row.getUserName()),
                        safe(row.getUnitName()),
                        safe(row.getAction()),
                        safe(row.getIpAddress()),
                        safe(row.getDevice()),
                        safe(row.getStatus()),
                        safe(row.getFailureReason()),
                        formatInstant(row.getCreatedAt())
                    ))
                    .toList()
            );
        }
        if ("system".equalsIgnoreCase(type)) {
            List<SystemLogResponse> rows = getSystemLogs(q, level, module, action, period, from, to, Pageable.unpaged()).getContent();
            return ExcelExportUtil.buildWorkbook(
                "system_logs",
                List.of("ID", "Actor ID", "Nguoi thuc hien", "Module", "Hanh dong", "Mo ta", "Muc do", "Metadata", "Thoi gian"),
                rows.stream()
                    .map(row -> List.of(
                        safe(row.getId()),
                        safe(row.getActorId()),
                        safe(row.getActorName()),
                        safe(row.getModule()),
                        safe(row.getAction()),
                        safe(row.getDescription()),
                        safe(row.getLevel()),
                        safe(row.getMetadata()),
                        formatInstant(row.getCreatedAt())
                    ))
                    .toList()
            );
        }
        throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Loai nhat ky khong hop le");
    }

    private Specification<SystemAuditLog> buildLoginSpecification(String q, String status, String action, TimeRange timeRange) {
        Specification<SystemAuditLog> specification = Specification.<SystemAuditLog>where((root, query, builder) -> builder.isNull(root.get("deletedAt")))
            .and((root, query, builder) -> builder.equal(root.get("actionType"), "LOGIN"))
            .and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("createdAt"), timeRange.from()))
            .and((root, query, builder) -> builder.lessThan(root.get("createdAt"), timeRange.to()));
        if (status != null && !status.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(builder.upper(root.get("status")), status.toUpperCase(Locale.ROOT)));
        }
        if (action != null && !action.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(builder.upper(root.get("actionType")), action.toUpperCase(Locale.ROOT)));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("actorName")), like),
                builder.like(builder.lower(root.get("entityName")), like),
                builder.like(builder.lower(root.get("detail")), like)
            ));
        }
        return specification;
    }

    private Specification<SystemAuditLog> buildSystemSpecification(String q, String level, String module, String action, TimeRange timeRange) {
        Specification<SystemAuditLog> specification = Specification.<SystemAuditLog>where((root, query, builder) -> builder.isNull(root.get("deletedAt")))
            .and((root, query, builder) -> builder.notEqual(root.get("actionType"), "LOGIN"))
            .and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("createdAt"), timeRange.from()))
            .and((root, query, builder) -> builder.lessThan(root.get("createdAt"), timeRange.to()));
        if (module != null && !module.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(builder.upper(root.get("moduleName")), module.toUpperCase(Locale.ROOT)));
        }
        if (action != null && !action.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(builder.upper(root.get("actionType")), action.toUpperCase(Locale.ROOT)));
        }
        if (level != null && !level.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(builder.upper(root.get("status")), mapLevelToStatus(level)));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("actorName")), like),
                builder.like(builder.lower(root.get("moduleName")), like),
                builder.like(builder.lower(root.get("entityName")), like),
                builder.like(builder.lower(root.get("detail")), like)
            ));
        }
        return specification;
    }

    private LoginLogResponse toLoginLogResponse(SystemAuditLog log, Map<Long, UserProfile> profiles) {
        Map<String, String> detail = AuditDetailUtil.decode(log.getDetail());
        Long actorId = log.getActorUser() != null ? log.getActorUser().getId() : null;
        UserProfile profile = actorId == null ? null : profiles.get(actorId);
        return new LoginLogResponse(
            log.getId(),
            actorId,
            log.getActorName(),
            profile == null ? null : profile.getUnitName(),
            log.getActionType(),
            detail.get("ip"),
            detail.get("device"),
            log.getStatus(),
            detail.get("failure_reason"),
            log.getCreatedAt()
        );
    }

    private SystemLogResponse toSystemLogResponse(SystemAuditLog log) {
        return new SystemLogResponse(
            log.getId(),
            log.getActorUser() != null ? log.getActorUser().getId() : null,
            log.getActorName(),
            log.getModuleName(),
            log.getActionType(),
            auditLogMessageService.buildMessage(log),
            mapStatusToLevel(log.getStatus()),
            auditLogMessageService.buildMetadata(log),
            log.getCreatedAt()
        );
    }

    private Map<Long, UserProfile> loadProfiles(List<SystemAuditLog> logs) {
        Set<Long> userIds = logs.stream()
            .map(SystemAuditLog::getActorUser)
            .filter(java.util.Objects::nonNull)
            .map(actorUser -> actorUser.getId())
            .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, UserProfile> profiles = new HashMap<>();
        for (UserProfile profile : userProfileRepository.findAllByUserIdInAndDeletedAtIsNull(userIds)) {
            profiles.put(profile.getUser().getId(), profile);
        }
        return profiles;
    }

    private String mapLevelToStatus(String level) {
        return switch (level.toUpperCase(Locale.ROOT)) {
            case "ERROR" -> "FAILED";
            case "WARN" -> "WARN";
            default -> "SUCCESS";
        };
    }

    private String mapStatusToLevel(String status) {
        if ("FAILED".equalsIgnoreCase(status)) {
            return "ERROR";
        }
        if ("WARN".equalsIgnoreCase(status)) {
            return "WARN";
        }
        return "INFO";
    }

    private String formatInstant(Instant instant) {
        return instant == null ? "" : DATE_TIME_FORMATTER.format(instant);
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
