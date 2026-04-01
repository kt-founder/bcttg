package com.bcttg.module.content.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.module.content.dto.ContentItemResponse;
import com.bcttg.module.content.entity.ContentItem;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.content.service.ContentItemService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/public/content-items")
@Tag(name = "Content Items (Public)", description = "Noi dung cong khai")
public class PublicContentItemController {
    private final ContentItemService service;

    public PublicContentItemController(ContentItemService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<List<ContentItemResponse>> list(
        @RequestParam(required = false, name = "category_id") Long categoryId,
        @RequestParam(required = false) ContentType type,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<ContentItem> result = service.findAllPublic(categoryId, type, q, pageable);
        List<ContentItemResponse> data = result.map(ContentItemResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ApiResponse<ContentItemResponse> get(@PathVariable Long id, Authentication authentication) {
        String actorPhone = authentication != null ? authentication.getName() : null;
        return ApiResponse.success(new ContentItemResponse(service.incrementViewCount(id, actorPhone)));
    }
}
