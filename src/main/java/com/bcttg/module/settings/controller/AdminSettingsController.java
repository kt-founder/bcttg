package com.bcttg.module.settings.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.module.settings.dto.SystemSettingsRequest;
import com.bcttg.module.settings.dto.SystemSettingsResponse;
import com.bcttg.module.settings.dto.SystemStatusCardResponse;
import com.bcttg.module.settings.dto.SystemVersionResponse;
import com.bcttg.module.settings.service.SystemSettingsService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/settings")
@Tag(name = "Settings (Admin)", description = "Quan ly cau hinh he thong")
public class AdminSettingsController {
    private final SystemSettingsService service;

    public AdminSettingsController(SystemSettingsService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemSettingsResponse> getSettings() {
        return ApiResponse.success(service.getSettings());
    }

    @PatchMapping
    @PutMapping
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

    private String actorPhone(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
