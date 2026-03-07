package com.bcttg.module.home.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.module.home.dto.HomeModuleResponse;
import com.bcttg.module.home.dto.UpdateHomeModulesRequest;
import com.bcttg.module.home.service.HomeModuleService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/home-modules")
@Tag(name = "Home Modules (Admin)", description = "Quan ly cau hinh module trang chu")
public class AdminHomeModuleController {
    private final HomeModuleService service;

    public AdminHomeModuleController(HomeModuleService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<HomeModuleResponse>> list() {
        return ApiResponse.success(service.getAdminModules());
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<HomeModuleResponse>> saveAll(@Valid @RequestBody UpdateHomeModulesRequest request, Authentication authentication) {
        return ApiResponse.success(service.saveAll(request, actorPhone(authentication)));
    }

    private String actorPhone(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
