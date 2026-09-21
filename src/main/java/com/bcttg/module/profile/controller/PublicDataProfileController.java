package com.bcttg.module.profile.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.module.profile.dto.DataProfileResponse;
import com.bcttg.module.profile.entity.DataProfile;
import com.bcttg.module.profile.entity.ProfileType;
import com.bcttg.module.profile.service.DataProfileService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/public/data-profiles")
@Tag(name = "Data Profiles (Public)", description = "Ho so du lieu cong khai Thu truong, Chien si, Anh hung")
public class PublicDataProfileController {
    private final DataProfileService service;

    public PublicDataProfileController(DataProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<DataProfileResponse>> list(
        @RequestParam(required = false) ProfileType profileType,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order,
        Authentication authentication
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<DataProfile> result = service.findAllPublic(profileType, q, pageable, authentication);
        List<DataProfileResponse> data = result.map(DataProfileResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<DataProfileResponse> get(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success(new DataProfileResponse(service.getVisibleById(id, authentication)));
    }
}
