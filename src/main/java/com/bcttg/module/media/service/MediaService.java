package com.bcttg.module.media.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.content.repository.ContentItemRepository;
import com.bcttg.module.dashboard.service.SystemAuditTrailService;
import com.bcttg.module.media.MediaProperties;
import com.bcttg.module.media.entity.MediaAsset;
import com.bcttg.module.media.repository.MediaAssetRepository;
import com.bcttg.module.song.repository.SongRepository;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {
    private final MediaAssetRepository mediaRepository;
    private final ContentItemRepository contentItemRepository;
    private final SongRepository songRepository;
    private final MediaProperties properties;
    private final SystemAuditTrailService auditTrailService;

    public MediaService(
        MediaAssetRepository mediaRepository,
        ContentItemRepository contentItemRepository,
        SongRepository songRepository,
        MediaProperties properties,
        SystemAuditTrailService auditTrailService
    ) {
        this.mediaRepository = mediaRepository;
        this.contentItemRepository = contentItemRepository;
        this.songRepository = songRepository;
        this.properties = properties;
        this.auditTrailService = auditTrailService;
    }

    @Transactional
    public MediaAsset upload(MultipartFile file, String createdBy) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "File is required");
        }
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storageKey = datePath + "/" + UUID.randomUUID() + "_" + safeName;

        Path root = Paths.get(properties.getStorageRoot()).toAbsolutePath().normalize();
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Invalid storage path");
        }
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }

        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String url = baseUrl + "/files/" + storageKey.replace("\\", "/");

        MediaAsset asset = new MediaAsset();
        asset.setFileName(originalName);
        asset.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        asset.setSizeBytes(file.getSize());
        asset.setStorageKey(storageKey);
        asset.setUrl(url);
        asset.setCreatedBy(createdBy);
        MediaAsset saved = mediaRepository.save(asset);
        auditTrailService.record(createdBy, "CREATE", "MEDIA", saved.getFileName(), "tải lên tệp media “" + saved.getFileName() + "”");
        return saved;
    }

    public MediaAsset getById(Long id) {
        MediaAsset media = mediaRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Media not found"));
        if (media.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Media not found");
        }
        return media;
    }

    @Transactional
    public void delete(Long id, String actorPhone) {
        MediaAsset media = getById(id);
        if (contentItemRepository.existsByCoverMediaAndDeletedAtIsNull(media)) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Media is used by content items");
        }
        if (songRepository.existsByAudioMediaAndDeletedAtIsNull(media)) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Media is used by songs");
        }
        String fileName = media.getFileName();
        mediaRepository.delete(media);
        auditTrailService.record(actorPhone, "DELETE", "MEDIA", fileName, "xóa tệp media “" + fileName + "”");
    }

    public Resource loadAsResource(String storageKey) {
        Path root = Paths.get(properties.getStorageRoot()).toAbsolutePath().normalize();
        Path filePath = root.resolve(storageKey).normalize();
        if (!filePath.startsWith(root)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        if (!Files.exists(filePath)) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "File not found");
        }
        try {
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "File not accessible");
        }
    }
}
