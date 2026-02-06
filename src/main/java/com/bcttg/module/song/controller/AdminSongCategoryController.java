package com.bcttg.module.song.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.common.VisibilityRequest;
import com.bcttg.module.song.dto.CreateSongCategoryRequest;
import com.bcttg.module.song.dto.ReorderSongCategoryRequest;
import com.bcttg.module.song.dto.SongCategoryResponse;
import com.bcttg.module.song.dto.UpdateSongCategoryRequest;
import com.bcttg.module.song.entity.SongCategory;
import com.bcttg.module.song.service.SongCategoryService;

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
@RequestMapping("/api/v1/admin/song-categories")
@Tag(name = "Song Categories (Admin)", description = "Quan ly danh muc ca khuc")
public class AdminSongCategoryController {
    private final SongCategoryService service;

    public AdminSongCategoryController(SongCategoryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<List<SongCategoryResponse>> list(
        @RequestParam(required = false, name = "parent_id") Long parentId,
        @RequestParam(required = false) String q,
        @RequestParam(required = false, name = "is_visible") Boolean isVisible,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<SongCategory> result = service.findAll(parentId, q, isVisible, pageable);
        List<SongCategoryResponse> data = result.map(SongCategoryResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<SongCategoryResponse> create(@Valid @RequestBody CreateSongCategoryRequest request) {
        return ApiResponse.success(new SongCategoryResponse(service.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<SongCategoryResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new SongCategoryResponse(service.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<SongCategoryResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateSongCategoryRequest request) {
        return ApiResponse.success(new SongCategoryResponse(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<SongCategoryResponse> updateVisibility(@PathVariable Long id, @Valid @RequestBody VisibilityRequest request) {
        return ApiResponse.success(new SongCategoryResponse(service.updateVisibility(id, request.getIsVisible())));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> reorder(@Valid @RequestBody ReorderSongCategoryRequest request) {
        service.reorder(request);
        return ApiResponse.success(null);
    }
}
