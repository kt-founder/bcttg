package com.bcttg.module.content.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.common.VisibilityRequest;
import com.bcttg.module.content.dto.ContentItemResponse;
import com.bcttg.module.content.dto.CreateContentItemRequest;
import com.bcttg.module.content.dto.ReorderContentItemRequest;
import com.bcttg.module.content.dto.UpdateContentItemRequest;
import com.bcttg.module.content.entity.ContentItem;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.content.service.ContentItemService;
import com.bcttg.module.media.dto.MediaResponse;
import com.bcttg.module.media.service.MediaService;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/content-items")
@Tag(name = "Content Items (Admin)", description = "Quan ly noi dung")
public class AdminContentItemController {
    private final ContentItemService service;
    private final MediaService mediaService;

    public AdminContentItemController(ContentItemService service, MediaService mediaService) {
        this.service = service;
        this.mediaService = mediaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<ContentItemResponse>> list(
        @RequestParam(required = false, name = "category_id") Long categoryId,
        @RequestParam(required = false) ContentType type,
        @RequestParam(required = false) String q,
        @RequestParam(required = false, name = "is_visible") Boolean isVisible,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("sortOrder").ascending());
        Page<ContentItem> result = service.findAll(categoryId, type, q, isVisible, pageable);
        List<ContentItemResponse> data = result.map(ContentItemResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ContentItemResponse> create(@Valid @RequestBody CreateContentItemRequest request, Authentication authentication) {
        return ApiResponse.success(new ContentItemResponse(service.create(request, actorPhone(authentication))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ContentItemResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new ContentItemResponse(service.getById(id)));
    }

    @RequestMapping(path = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ContentItemResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateContentItemRequest request, Authentication authentication) {
        return ApiResponse.success(new ContentItemResponse(service.update(id, request, actorPhone(authentication))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, actorPhone(authentication));
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ContentItemResponse> updateVisibility(@PathVariable Long id, @Valid @RequestBody VisibilityRequest request, Authentication authentication) {
        return ApiResponse.success(new ContentItemResponse(service.updateVisibility(id, request.getIsVisible(), actorPhone(authentication))));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> reorder(@Valid @RequestBody ReorderContentItemRequest request, Authentication authentication) {
        service.reorder(request, actorPhone(authentication));
        return ApiResponse.success(null);
    }

    @PostMapping(value = "/{id}/media", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<MediaResponse> uploadMedia(@PathVariable Long id, @RequestPart("file") MultipartFile file, Authentication authentication) {
        service.getById(id);
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.success(new MediaResponse(mediaService.upload(file, username)));
    }

    private String actorPhone(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
