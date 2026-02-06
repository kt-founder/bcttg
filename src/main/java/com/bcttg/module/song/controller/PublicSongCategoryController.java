package com.bcttg.module.song.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.module.song.dto.SongCategoryResponse;
import com.bcttg.module.song.entity.SongCategory;
import com.bcttg.module.song.service.SongCategoryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/public/song-categories")
@Tag(name = "Song Categories (Public)", description = "Danh muc ca khuc cong khai")
public class PublicSongCategoryController {
    private final SongCategoryService service;

    public PublicSongCategoryController(SongCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SongCategoryResponse>> list(
        @RequestParam(required = false, name = "parent_id") Long parentId,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<SongCategory> result = service.findAllPublic(parentId, q, pageable);
        List<SongCategoryResponse> data = result.map(SongCategoryResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<SongCategoryResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new SongCategoryResponse(service.getVisibleById(id)));
    }
}
