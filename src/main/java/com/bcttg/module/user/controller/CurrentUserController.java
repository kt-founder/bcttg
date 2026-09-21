package com.bcttg.module.user.controller;

import com.bcttg.common.ApiResponse;
import com.bcttg.module.user.dto.UserAdminResponse;
import com.bcttg.module.user.service.UserManagementService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "Current User", description = "Thong tin tai khoan dang dang nhap")
public class CurrentUserController {
    private final UserManagementService userManagementService;

    public CurrentUserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<UserAdminResponse> me(Authentication authentication) {
        String actorPhone = authentication != null ? authentication.getName() : null;
        return ApiResponse.success(userManagementService.getCurrentUser(actorPhone));
    }
}
