package com.bcttg.module.user.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.module.user.dto.CreateUserRequest;
import com.bcttg.module.user.dto.ResetUserPasswordRequest;
import com.bcttg.module.user.dto.UpdateUserActiveRequest;
import com.bcttg.module.user.dto.UpdateUserRequest;
import com.bcttg.module.user.dto.UpdateUserRoleRequest;
import com.bcttg.module.user.dto.UserAdminResponse;
import com.bcttg.module.user.entity.UserRole;
import com.bcttg.module.user.service.UserManagementService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Users (Admin)", description = "Quan ly tai khoan nguoi dung")
public class AdminUserController {
    private final UserManagementService userManagementService;

    public AdminUserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<UserAdminResponse>> list(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) UserRole role,
        @RequestParam(required = false, name = "is_active") Boolean isActive,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("createdAt").descending());
        Page<UserAdminResponse> result = userManagementService.findAll(q, role, isActive, pageable);
        return ApiResponse.success(result.getContent(), PageMeta.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<UserAdminResponse> get(@PathVariable Long id) {
        return ApiResponse.success(userManagementService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserAdminResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(userManagementService.create(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserAdminResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request, Authentication authentication) {
        return ApiResponse.success(userManagementService.update(id, request, actorPhone(authentication)));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserAdminResponse> updateActive(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserActiveRequest request,
        Authentication authentication
    ) {
        return ApiResponse.success(userManagementService.updateActive(id, request.getValue(), actorPhone(authentication)));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserAdminResponse> updateRole(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRoleRequest request,
        Authentication authentication
    ) {
        return ApiResponse.success(userManagementService.updateRole(id, request.getRole(), actorPhone(authentication)));
    }

    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetUserPasswordRequest request) {
        userManagementService.resetPassword(id, request.getNewPassword());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        userManagementService.delete(id, actorPhone(authentication));
        return ApiResponse.success(null);
    }

    private String actorPhone(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
