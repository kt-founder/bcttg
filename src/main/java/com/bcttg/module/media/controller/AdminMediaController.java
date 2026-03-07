package com.bcttg.module.media.controller;

import com.bcttg.common.ApiResponse;
import com.bcttg.module.media.dto.MediaResponse;
import com.bcttg.module.media.service.MediaService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/media")
@Tag(name = "Media (Admin)", description = "Quan ly tep media")
public class AdminMediaController {
    private final MediaService mediaService;

    public AdminMediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<MediaResponse> upload(@RequestPart("file") MultipartFile file, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.success(new MediaResponse(mediaService.upload(file, username)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<MediaResponse> get(@PathVariable Long id) {
        return ApiResponse.success(new MediaResponse(mediaService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ApiResponse.success(null);
    }
}
