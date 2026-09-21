package com.bcttg.module.dashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.bcttg.common.AuditDetailUtil;
import com.bcttg.module.dashboard.entity.SystemAuditLog;

import org.springframework.stereotype.Service;

@Service
public class AuditLogMessageService {
    public String buildMessage(SystemAuditLog log) {
        String actorName = resolveActorName(log);
        String actionText = resolveActionText(log);
        return actorName + " vừa " + actionText;
    }

    public String buildMetadata(SystemAuditLog log) {
        Map<String, String> detailValues = AuditDetailUtil.decode(log.getDetail());
        if (!detailValues.isEmpty()) {
            List<String> parts = new ArrayList<>();
            appendDetail(parts, "IP", detailValues.get("ip"));
            appendDetail(parts, "Thiết bị", detailValues.get("device"));
            appendDetail(parts, "Lý do", normalizeFailureReason(detailValues.get("failure_reason")));
            appendDetail(parts, "Mã nội dung", detailValues.get("content_id"));
            return parts.isEmpty() ? null : String.join(" • ", parts);
        }
        return null;
    }

    public String buildTitle(SystemAuditLog log) {
        return switch (normalize(log.getActionType())) {
            case "LOGIN" -> "Đăng nhập";
            case "VIEW" -> "Xem dữ liệu";
            case "LISTEN" -> "Nghe ca khúc";
            case "CREATE" -> "Tạo mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "RESTORE" -> "Khôi phục";
            case "CLEAR_CACHE" -> "Xóa bộ nhớ đệm";
            case "TEST_EMAIL" -> "Gửi email kiểm tra";
            default -> "Hoạt động hệ thống";
        };
    }

    private void appendDetail(List<String> parts, String label, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(label + ": " + value);
        }
    }

    private String resolveActorName(SystemAuditLog log) {
        String actorName = log.getActorName();
        if (actorName == null || actorName.isBlank()) {
            return "Hệ thống";
        }
        return actorName;
    }

    private String resolveActionText(SystemAuditLog log) {
        Map<String, String> detailValues = AuditDetailUtil.decode(log.getDetail());
        String actionType = normalize(log.getActionType());
        String moduleName = normalize(log.getModuleName());
        if ("LOGIN".equals(actionType)) {
            return "FAILED".equalsIgnoreCase(log.getStatus())
                ? "đăng nhập thất bại"
                : "đăng nhập";
        }
        if ("VIEW".equals(actionType) && "CONTENT".equals(moduleName)) {
            return "xem nội dung " + wrapEntity(log.getEntityName());
        }
        if ("LISTEN".equals(actionType) && "SONG".equals(moduleName)) {
            return "nghe ca khúc " + wrapEntity(log.getEntityName());
        }
        if ("SETTINGS".equals(moduleName) && "TEST_EMAIL".equals(actionType)) {
            return "gửi email kiểm tra tới " + wrapEntity(log.getEntityName());
        }
        if ("SETTINGS".equals(moduleName) && "RESTORE".equals(actionType)) {
            return "khôi phục bản sao lưu " + wrapEntity(log.getEntityName());
        }
        if ("SETTINGS".equals(moduleName) && "CLEAR_CACHE".equals(actionType)) {
            return "xóa bộ nhớ đệm";
        }

        String detail = normalizePlainDetail(log.getDetail());
        if (detail != null && !detail.isBlank()) {
            return detail;
        }

        if ("CONTENT".equals(moduleName)) {
            return buildEntityPhrase(actionType, "nội dung", log.getEntityName());
        }
        if ("CONTENT_CATEGORY".equals(moduleName)) {
            return buildEntityPhrase(actionType, "danh mục nội dung", log.getEntityName());
        }
        if ("SONG".equals(moduleName)) {
            return buildEntityPhrase(actionType, "ca khúc", log.getEntityName());
        }
        if ("SONG_CATEGORY".equals(moduleName)) {
            return buildEntityPhrase(actionType, "danh mục ca khúc", log.getEntityName());
        }
        if ("DATA_PROFILE".equals(moduleName)) {
            return buildEntityPhrase(actionType, "hồ sơ dữ liệu", log.getEntityName());
        }
        if ("USER".equals(moduleName)) {
            return buildEntityPhrase(actionType, "tài khoản", log.getEntityName());
        }
        if ("MEDIA".equals(moduleName)) {
            return buildEntityPhrase(actionType, "tệp media", log.getEntityName());
        }
        if ("HOME_MODULE".equals(moduleName)) {
            return "cập nhật cấu hình module trang chủ";
        }
        if ("SETTINGS".equals(moduleName)) {
            return "cập nhật cấu hình hệ thống";
        }
        if (!detailValues.isEmpty()) {
            String contentId = detailValues.get("content_id");
            if (contentId != null && !contentId.isBlank()) {
                return "xem nội dung " + wrapEntity(log.getEntityName());
            }
        }
        return buildEntityPhrase(actionType, "dữ liệu", log.getEntityName());
    }

    private String buildEntityPhrase(String actionType, String entityLabel, String entityName) {
        String wrappedEntity = wrapEntity(entityName);
        return switch (actionType) {
            case "CREATE" -> "tạo " + entityLabel + " " + wrappedEntity;
            case "UPDATE" -> "chỉnh sửa " + entityLabel + " " + wrappedEntity;
            case "DELETE" -> "xóa " + entityLabel + " " + wrappedEntity;
            default -> "cập nhật " + entityLabel + " " + wrappedEntity;
        };
    }

    private String normalizePlainDetail(String detail) {
        if (detail == null || detail.isBlank() || detail.contains("=")) {
            return null;
        }
        String trimmed = detail.trim();
        return switch (trimmed) {
            case "Cap nhat cau hinh he thong" -> "cập nhật cấu hình hệ thống";
            case "Khoi phuc cau hinh mac dinh" -> "khôi phục cấu hình mặc định";
            case "Cap nhat cau hinh module trang chu" -> "cập nhật cấu hình module trang chủ";
            case "Gui email kiem tra" -> "gửi email kiểm tra";
            case "Phuc hoi backup" -> "khôi phục bản sao lưu";
            case "Khong co cache de xoa", "Da xoa cache", "Khong co bo nho dem de xoa" -> "xóa bộ nhớ đệm";
            default -> trimmed;
        };
    }

    private String normalizeFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return null;
        }
        return switch (failureReason) {
            case "Bad credentials" -> "Sai số điện thoại hoặc mật khẩu";
            case "User is inactive" -> "Tài khoản đang bị khóa";
            default -> failureReason;
        };
    }

    private String wrapEntity(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            return "không xác định";
        }
        return "“" + entityName + "”";
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.ROOT);
    }
}
