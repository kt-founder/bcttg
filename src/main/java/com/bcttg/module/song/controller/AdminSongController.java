package com.bcttg.module.song.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.common.VisibilityRequest;
import com.bcttg.module.song.dto.CreateSongRequest;
import com.bcttg.module.song.dto.ReorderSongRequest;
import com.bcttg.module.song.dto.SongResponse;
import com.bcttg.module.song.dto.UpdateSongRequest;
import com.bcttg.module.song.entity.Song;
import com.bcttg.module.song.service.SongService;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/songs")
@Tag(name = "Songs (Admin)", description = "Quan ly ca khuc")
public class AdminSongController {
    private final SongService service;

    public AdminSongController(SongService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<SongResponse>> list(
        @RequestParam(required = false, name = "category_id") Long categoryId,
        @RequestParam(required = false) String q,
        @RequestParam(required = false, name = "is_visible") Boolean isVisible,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<Song> result = service.findAll(categoryId, q, isVisible, pageable);
        List<SongResponse> data = result.map(SongResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<SongResponse> create(@Valid @RequestBody CreateSongRequest request) {
        return ApiResponse.success(new SongResponse(service.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<SongResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new SongResponse(service.getById(id)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<SongResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateSongRequest request) {
        return ApiResponse.success(new SongResponse(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<SongResponse> updateVisibility(@PathVariable Long id, @Valid @RequestBody VisibilityRequest request) {
        return ApiResponse.success(new SongResponse(service.updateVisibility(id, request.getIsVisible())));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> reorder(@Valid @RequestBody ReorderSongRequest request) {
        service.reorder(request);
        return ApiResponse.success(null);
    }
}
