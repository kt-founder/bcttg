package com.bcttg.module.song.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.dashboard.service.SystemAuditTrailService;
import com.bcttg.module.media.entity.MediaAsset;
import com.bcttg.module.media.repository.MediaAssetRepository;
import com.bcttg.module.song.dto.CreateSongRequest;
import com.bcttg.module.song.dto.ReorderSongRequest;
import com.bcttg.module.song.dto.UpdateSongRequest;
import com.bcttg.module.song.entity.Song;
import com.bcttg.module.song.entity.SongCategory;
import com.bcttg.module.song.repository.SongCategoryRepository;
import com.bcttg.module.song.repository.SongRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SongService {
    private final SongRepository songRepository;
    private final SongCategoryRepository categoryRepository;
    private final MediaAssetRepository mediaRepository;
    private final SystemAuditTrailService auditTrailService;

    public SongService(
        SongRepository songRepository,
        SongCategoryRepository categoryRepository,
        MediaAssetRepository mediaRepository,
        SystemAuditTrailService auditTrailService
    ) {
        this.songRepository = songRepository;
        this.categoryRepository = categoryRepository;
        this.mediaRepository = mediaRepository;
        this.auditTrailService = auditTrailService;
    }

    public Page<Song> findAll(Long categoryId, String q, Boolean isVisible, Pageable pageable) {
        Specification<Song> spec = Specification.<Song>where(
            (root, query, cb) -> cb.isNull(root.get("deletedAt"))
        );
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), like));
        }
        if (isVisible != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isVisible"), isVisible));
        }
        return songRepository.findAll(spec, pageable);
    }

    public Page<Song> findAllPublic(Long categoryId, String q, Pageable pageable) {
        return findAll(categoryId, q, true, pageable);
    }

    public Song getById(Long id) {
        Song song = songRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song not found"));
        if (song.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song not found");
        }
        return song;
    }

    public Song getVisibleById(Long id) {
        Song song = getById(id);
        if (!Boolean.TRUE.equals(song.getIsVisible())) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song not found");
        }
        return song;
    }

    @Transactional
    public Song create(CreateSongRequest request, String actorPhone) {
        SongCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song category not found"));
        }
        MediaAsset audioMedia = null;
        if (request.getAudioMediaId() != null) {
            audioMedia = mediaRepository.findById(request.getAudioMediaId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Audio media not found"));
        }
        validateAudioSource(request.getAudioMediaId(), request.getAudioUrl());

        Song song = new Song();
        song.setCategory(category);
        song.setTitle(request.getTitle());
        song.setLyric(request.getLyric());
        song.setAudioMedia(audioMedia);
        song.setAudioUrl(request.getAudioUrl());
        song.setDurationSec(request.getDurationSec());
        song.setIsVisible(Optional.ofNullable(request.getIsVisible()).orElse(true));
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            sortOrder = songRepository.findMaxSortOrder(category) + 1;
        }
        song.setSortOrder(sortOrder);
        Song saved = songRepository.save(song);
        auditTrailService.record(actorPhone, "CREATE", "SONG", saved.getTitle(), "tạo ca khúc “" + saved.getTitle() + "”");
        return saved;
    }

    @Transactional
    public Song update(Long id, UpdateSongRequest request, String actorPhone) {
        Song song = getById(id);
        SongCategory category = song.getCategory();
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song category not found"));
        }
        MediaAsset audioMedia = song.getAudioMedia();
        String audioUrl = song.getAudioUrl();
        if (request.getAudioMediaId() != null || request.getAudioUrl() != null) {
            validateAudioSource(request.getAudioMediaId(), request.getAudioUrl());
            if (request.getAudioMediaId() != null) {
                audioMedia = mediaRepository.findById(request.getAudioMediaId())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Audio media not found"));
                audioUrl = null;
            } else {
                audioMedia = null;
                audioUrl = request.getAudioUrl();
            }
        }
        if (request.getAudioMediaId() != null && audioMedia == null) {
            audioMedia = mediaRepository.findById(request.getAudioMediaId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Audio media not found"));
        }

        song.setCategory(category);
        song.setTitle(valueOrDefault(request.getTitle(), song.getTitle()));
        song.setLyric(valueOrDefault(request.getLyric(), song.getLyric()));
        song.setAudioMedia(audioMedia);
        song.setAudioUrl(audioUrl);
        if (request.getDurationSec() != null) {
            song.setDurationSec(request.getDurationSec());
        }
        if (request.getIsVisible() != null) {
            song.setIsVisible(request.getIsVisible());
        }
        if (request.getSortOrder() != null) {
            song.setSortOrder(request.getSortOrder());
        }
        Song saved = songRepository.save(song);
        auditTrailService.record(actorPhone, "UPDATE", "SONG", saved.getTitle(), "chỉnh sửa ca khúc “" + saved.getTitle() + "”");
        return saved;
    }

    @Transactional
    public void delete(Long id, String actorPhone) {
        Song song = getById(id);
        String title = song.getTitle();
        songRepository.delete(song);
        auditTrailService.record(actorPhone, "DELETE", "SONG", title, "xóa ca khúc “" + title + "”");
    }

    @Transactional
    public Song updateVisibility(Long id, boolean isVisible, String actorPhone) {
        Song song = getById(id);
        song.setIsVisible(isVisible);
        Song saved = songRepository.save(song);
        String phrase = isVisible ? "hiển thị ca khúc “" + saved.getTitle() + "”" : "ẩn ca khúc “" + saved.getTitle() + "”";
        auditTrailService.record(actorPhone, "UPDATE", "SONG", saved.getTitle(), phrase);
        return saved;
    }

    @Transactional
    public void reorder(ReorderSongRequest request, String actorPhone) {
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Orders cannot be empty");
        }
        SongCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song category not found"));
        }
        List<Long> ids = request.getOrders().stream().map(ReorderSongRequest.OrderItem::getId).toList();
        List<Song> songs = songRepository.findAllById(ids);
        if (songs.size() != ids.size()) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some songs not found");
        }
        for (Song song : songs) {
            if (song.getDeletedAt() != null) {
                throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some songs not found");
            }
            if (category == null) {
                if (song.getCategory() != null) {
                    throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Song category mismatch in reorder scope");
                }
            } else if (song.getCategory() == null || !song.getCategory().getId().equals(category.getId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Song category mismatch in reorder scope");
            }
        }
        for (ReorderSongRequest.OrderItem order : request.getOrders()) {
            songs.stream()
                .filter(s -> s.getId().equals(order.getId()))
                .findFirst()
                .ifPresent(s -> s.setSortOrder(order.getSortOrder()));
        }
        songRepository.saveAll(songs);
        String scopeName = category == null ? "Tất cả ca khúc" : category.getName();
        auditTrailService.record(actorPhone, "UPDATE", "SONG", scopeName, "sắp xếp lại ca khúc");
    }

    private void validateAudioSource(Long audioMediaId, String audioUrl) {
        boolean hasMedia = audioMediaId != null;
        boolean hasUrl = audioUrl != null && !audioUrl.isBlank();
        if (hasMedia == hasUrl) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Provide exactly one of audio_media_id or audio_url");
        }
    }

    private <T> T valueOrDefault(T requestedValue, T currentValue) {
        return requestedValue != null ? requestedValue : currentValue;
    }
}
