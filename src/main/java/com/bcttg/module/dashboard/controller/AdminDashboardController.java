package com.bcttg.module.dashboard.controller;

import com.bcttg.common.ApiResponse;
import com.bcttg.module.dashboard.dto.AdminDashboardResponse;
import com.bcttg.module.dashboard.service.AdminDashboardService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Dashboard (Admin)", description = "Tong hop thong ke va trang thai he thong")
public class AdminDashboardController {
    private final AdminDashboardService service;

    public AdminDashboardController(AdminDashboardService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<AdminDashboardResponse> overview() {
        return ApiResponse.success(service.getOverview());
    }
}
