package com.bcttg.module.report.controller;

import com.bcttg.common.ApiResponse;
import com.bcttg.module.report.dto.ReportOverviewResponse;
import com.bcttg.module.report.service.AdminReportService;

import org.springframework.core.io.ByteArrayResource;
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
@RequestMapping("/api/v1/admin/reports")
@Tag(name = "Reports (Admin)", description = "Bao cao tong hop he thong")
public class AdminReportController {
    private final AdminReportService service;

    public AdminReportController(AdminReportService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ReportOverviewResponse> overview(@RequestParam(defaultValue = "month") String period) {
        return ApiResponse.success(service.getOverview(period));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ByteArrayResource> export(
        @RequestParam(defaultValue = "overview") String type,
        @RequestParam(defaultValue = "month") String period,
        @RequestParam(defaultValue = "xlsx") String format
    ) {
        ByteArrayResource resource = service.export(type, period, format);
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("report-" + type + ".xlsx").build().toString()
            )
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(resource);
    }
}
