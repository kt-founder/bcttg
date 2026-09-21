package com.bcttg.module.log.controller;

import java.time.Instant;
import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.module.log.dto.LogSummaryResponse;
import com.bcttg.module.log.dto.LoginLogResponse;
import com.bcttg.module.log.dto.SystemLogResponse;
import com.bcttg.module.log.service.AdminLogService;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/logs")
@Tag(name = "Logs (Admin)", description = "Nhat ky dang nhap va nhat ky he thong")
public class AdminLogController {
    private final AdminLogService service;

    public AdminLogController(AdminLogService service) {
        this.service = service;
    }

    @GetMapping("/login")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<LoginLogResponse>> loginLogs(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String period,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("createdAt").descending());
        Page<LoginLogResponse> result = service.getLoginLogs(q, status, action, period, from, to, pageable);
        return ApiResponse.success(result.getContent(), PageMeta.from(result));
    }

    @GetMapping("/system")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<SystemLogResponse>> systemLogs(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String level,
        @RequestParam(required = false) String module,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String period,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("createdAt").descending());
        Page<SystemLogResponse> result = service.getSystemLogs(q, level, module, action, period, from, to, pageable);
        return ApiResponse.success(result.getContent(), PageMeta.from(result));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<LogSummaryResponse> summary() {
        return ApiResponse.success(service.getSummary());
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ByteArrayResource> export(
        @RequestParam String type,
        @RequestParam(defaultValue = "xlsx") String format,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String level,
        @RequestParam(required = false) String module,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String period,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to
    ) {
        ByteArrayResource resource = service.exportLogs(type, format, q, status, level, module, action, period, from, to);
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("logs-" + type + ".xlsx").build().toString()
            )
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(resource);
    }
}
