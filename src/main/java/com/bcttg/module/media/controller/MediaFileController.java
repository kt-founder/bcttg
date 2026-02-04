package com.bcttg.module.media.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import com.bcttg.module.media.service.MediaService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.HandlerMapping;

@Controller
public class MediaFileController {
    private final MediaService mediaService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public MediaFileController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/files/**")
    public ResponseEntity<Resource> serve(HttpServletRequest request) throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatch = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String storageKey = pathMatcher.extractPathWithinPattern(bestMatch, path);
        Resource resource = mediaService.loadAsResource(storageKey);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            Path filePath = resource.getFile().toPath();
            String contentType = Files.probeContentType(filePath);
            if (contentType != null) {
                mediaType = MediaType.parseMediaType(contentType);
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }
}
