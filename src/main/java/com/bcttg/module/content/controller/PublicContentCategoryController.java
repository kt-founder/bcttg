package com.bcttg.module.content.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.module.content.dto.ContentCategoryResponse;
import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.content.service.ContentCategoryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/public/content-categories")
@Tag(name = "Content Categories (Public)", description = "Danh muc noi dung cong khai")
public class PublicContentCategoryController {
    private final ContentCategoryService service;

    public PublicContentCategoryController(ContentCategoryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<List<ContentCategoryResponse>> list(
        @RequestParam(required = false) ContentType type,
        @RequestParam(required = false, name = "parent_id") Long parentId,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<ContentCategory> result = service.findAllPublic(type, parentId, q, pageable);
        List<ContentCategoryResponse> data = result.map(ContentCategoryResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<ContentCategoryResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new ContentCategoryResponse(service.getVisibleById(id)));
    }
}
