package com.bcttg.module.home.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.module.home.dto.HomeModuleResponse;
import com.bcttg.module.home.service.HomeModuleService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/public/home-modules")
@Tag(name = "Home Modules (Public)", description = "Danh sach module trang chu cong khai")
public class PublicHomeModuleController {
    private final HomeModuleService service;

    public PublicHomeModuleController(HomeModuleService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<HomeModuleResponse>> list(Authentication authentication) {
        return ApiResponse.success(service.getPublicModules(authentication));
    }
}
