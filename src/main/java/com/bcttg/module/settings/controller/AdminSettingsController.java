package com.bcttg.module.settings.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.module.settings.dto.BackupFileResponse;
import com.bcttg.module.settings.dto.CacheClearResponse;
import com.bcttg.module.settings.dto.OperationMessageResponse;
import com.bcttg.module.settings.dto.SystemSettingsRequest;
import com.bcttg.module.settings.dto.SystemSettingsResponse;
import com.bcttg.module.settings.dto.SystemStatusCardResponse;
import com.bcttg.module.settings.dto.SystemVersionResponse;
import com.bcttg.module.settings.dto.TestEmailRequest;
import com.bcttg.module.settings.dto.TestEmailResponse;
import com.bcttg.module.settings.service.SettingsOperationsService;
import com.bcttg.module.settings.service.SystemSettingsService;

import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/settings")
@Tag(name = "Settings (Admin)", description = "Quan ly cau hinh he thong")
public class AdminSettingsController {
    private final SystemSettingsService service;
    private final SettingsOperationsService operationsService;

    public AdminSettingsController(SystemSettingsService service, SettingsOperationsService operationsService) {
        this.service = service;
        this.operationsService = operationsService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemSettingsResponse> getSettings() {
        return ApiResponse.success(service.getSettings());
    }

    @RequestMapping(method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemSettingsResponse> update(@Valid @RequestBody SystemSettingsRequest request, Authentication authentication) {
        return ApiResponse.success(service.update(request, actorPhone(authentication)));
    }

    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemSettingsResponse> reset(Authentication authentication) {
        return ApiResponse.success(service.reset(actorPhone(authentication)));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SystemStatusCardResponse>> status() {
        return ApiResponse.success(service.getStatusCards());
    }

    @GetMapping("/version")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemVersionResponse> version() {
        return ApiResponse.success(service.getVersion());
    }

    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TestEmailResponse> testEmail(@Valid @RequestBody TestEmailRequest request, Authentication authentication) {
        return ApiResponse.success(operationsService.testEmail(request, actorPhone(authentication)));
    }

    @GetMapping("/backups")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BackupFileResponse>> backups() {
        return ApiResponse.success(operationsService.listBackups());
    }

    @PostMapping("/backups/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OperationMessageResponse> restoreBackup(@PathVariable String id, Authentication authentication) {
        return ApiResponse.success(operationsService.restoreBackup(id, actorPhone(authentication)));
    }

    @GetMapping("/backups/{id}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String id) {
        Resource resource = operationsService.downloadBackup(id);
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(resource.getFilename()).build().toString()
            )
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CacheClearResponse> clearCache(Authentication authentication) {
        return ApiResponse.success(operationsService.clearCache(actorPhone(authentication)));
    }

    private String actorPhone(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
