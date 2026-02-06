package com.bcttg.module.content.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.common.VisibilityRequest;
import com.bcttg.module.content.dto.ContentCategoryResponse;
import com.bcttg.module.content.dto.CreateContentCategoryRequest;
import com.bcttg.module.content.dto.ReorderContentCategoryRequest;
import com.bcttg.module.content.dto.UpdateContentCategoryRequest;
import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.content.service.ContentCategoryService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/admin/content-categories")
@Tag(name = "Content Categories (Admin)", description = "Quan ly danh muc noi dung")
public class AdminContentCategoryController {
    private final ContentCategoryService service;

    public AdminContentCategoryController(ContentCategoryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<List<ContentCategoryResponse>> list(
        @RequestParam(required = false) ContentType type,
        @RequestParam(required = false, name = "parent_id") Long parentId,
        @RequestParam(required = false) String q,
        @RequestParam(required = false, name = "is_visible") Boolean isVisible,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<ContentCategory> result = service.findAll(type, parentId, q, isVisible, pageable);
        List<ContentCategoryResponse> data = result.map(ContentCategoryResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ContentCategoryResponse> create(@Valid @RequestBody CreateContentCategoryRequest request) {
        return ApiResponse.success(new ContentCategoryResponse(service.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<ContentCategoryResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new ContentCategoryResponse(service.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ContentCategoryResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateContentCategoryRequest request) {
        return ApiResponse.success(new ContentCategoryResponse(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ContentCategoryResponse> updateVisibility(@PathVariable Long id, @Valid @RequestBody VisibilityRequest request) {
        return ApiResponse.success(new ContentCategoryResponse(service.updateVisibility(id, request.getIsVisible())));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> reorder(@Valid @RequestBody ReorderContentCategoryRequest request) {
        service.reorder(request);
        return ApiResponse.success(null);
    }
}
