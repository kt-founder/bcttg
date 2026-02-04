package com.bcttg.module.song.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.module.song.dto.SongResponse;
import com.bcttg.module.song.entity.Song;
import com.bcttg.module.song.service.SongService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/songs")
public class PublicSongController {
    private final SongService service;

    public PublicSongController(SongService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SongResponse>> list(
        @RequestParam(required = false, name = "category_id") Long categoryId,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<Song> result = service.findAllPublic(categoryId, q, pageable);
        List<SongResponse> data = result.map(SongResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<SongResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new SongResponse(service.getVisibleById(id)));
    }
}
