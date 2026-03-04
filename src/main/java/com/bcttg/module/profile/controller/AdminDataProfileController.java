package com.bcttg.module.profile.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.common.VisibilityRequest;
import com.bcttg.module.profile.dto.CreateDataProfileRequest;
import com.bcttg.module.profile.dto.DataProfileResponse;
import com.bcttg.module.profile.dto.ReorderDataProfileRequest;
import com.bcttg.module.profile.dto.UpdateDataProfileRequest;
import com.bcttg.module.profile.entity.DataProfile;
import com.bcttg.module.profile.entity.ProfileType;
import com.bcttg.module.profile.service.DataProfileService;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/data-profiles")
@Tag(name = "Data Profiles (Admin)", description = "Quan ly ho so du lieu Thu truong, Chien si, Anh hung")
public class AdminDataProfileController {
    private final DataProfileService service;

    public AdminDataProfileController(DataProfileService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<List<DataProfileResponse>> list(
        @RequestParam(required = false) ProfileType profileType,
        @RequestParam(required = false) String q,
        @RequestParam(required = false, name = "is_visible") Boolean isVisible,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<DataProfile> result = service.findAll(profileType, q, isVisible, pageable);
        List<DataProfileResponse> data = result.map(DataProfileResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<DataProfileResponse> create(@Valid @RequestBody CreateDataProfileRequest request, Authentication authentication) {
        String phone = authentication != null ? authentication.getName() : null;
        return ApiResponse.success(new DataProfileResponse(service.create(request, phone)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<DataProfileResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new DataProfileResponse(service.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<DataProfileResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateDataProfileRequest request) {
        return ApiResponse.success(new DataProfileResponse(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<DataProfileResponse> updateVisibility(@PathVariable Long id, @Valid @RequestBody VisibilityRequest request) {
        return ApiResponse.success(new DataProfileResponse(service.updateVisibility(id, request.getIsVisible())));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> reorder(@Valid @RequestBody ReorderDataProfileRequest request) {
        service.reorder(request);
        return ApiResponse.success(null);
    }
}
