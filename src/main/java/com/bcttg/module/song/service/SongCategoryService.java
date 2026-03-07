package com.bcttg.module.song.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.song.dto.CreateSongCategoryRequest;
import com.bcttg.module.song.dto.ReorderSongCategoryRequest;
import com.bcttg.module.song.dto.UpdateSongCategoryRequest;
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
public class SongCategoryService {
    private final SongCategoryRepository categoryRepository;
    private final SongRepository songRepository;

    public SongCategoryService(SongCategoryRepository categoryRepository, SongRepository songRepository) {
        this.categoryRepository = categoryRepository;
        this.songRepository = songRepository;
    }

    public Page<SongCategory> findAll(Long parentId, String q, Boolean isVisible, Pageable pageable) {
        Specification<SongCategory> spec = Specification.<SongCategory>where(
            (root, query, cb) -> cb.isNull(root.get("deletedAt"))
        );
        if (parentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("parent").get("id"), parentId));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("slug")), like)
            ));
        }
        if (isVisible != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isVisible"), isVisible));
        }
        return categoryRepository.findAll(spec, pageable);
    }

    public Page<SongCategory> findAllPublic(Long parentId, String q, Pageable pageable) {
        return findAll(parentId, q, true, pageable);
    }

    public SongCategory getById(Long id) {
        SongCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song category not found"));
        if (category.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song category not found");
        }
        return category;
    }

    public SongCategory getVisibleById(Long id) {
        SongCategory category = getById(id);
        if (!Boolean.TRUE.equals(category.getIsVisible())) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Song category not found");
        }
        return category;
    }

    @Transactional
    public SongCategory create(CreateSongCategoryRequest request) {
        SongCategory parent = null;
        if (request.getParentId() != null) {
            parent = getById(request.getParentId());
            if (parent.getParent() != null) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Only 2 levels of song categories are allowed");
            }
        }

        if (categoryRepository.existsByParentAndSlugAndDeletedAtIsNull(parent, request.getSlug())) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Slug already exists in scope");
        }

        SongCategory category = new SongCategory();
        category.setParent(parent);
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setIsVisible(Optional.ofNullable(request.getIsVisible()).orElse(true));
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            sortOrder = categoryRepository.findMaxSortOrder(parent) + 1;
        }
        category.setSortOrder(sortOrder);
        return categoryRepository.save(category);
    }

    @Transactional
    public SongCategory update(Long id, UpdateSongCategoryRequest request) {
        SongCategory category = getById(id);
        SongCategory parent = category.getParent();
        if (request.getParentId() != null) {
            parent = getById(request.getParentId());
            if (parent.getParent() != null) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Only 2 levels of song categories are allowed");
            }
            if (parent.getId().equals(category.getId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Parent cannot be itself");
            }
        }

        String slug = valueOrDefault(request.getSlug(), category.getSlug());
        SongCategory existing = categoryRepository.findByParentAndSlug(parent, slug).orElse(null);
        if (existing != null && existing.getDeletedAt() == null && !existing.getId().equals(category.getId())) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Slug already exists in scope");
        }

        category.setParent(parent);
        category.setName(valueOrDefault(request.getName(), category.getName()));
        category.setSlug(slug);
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getIsVisible() != null) {
            category.setIsVisible(request.getIsVisible());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        SongCategory category = getById(id);
        if (categoryRepository.existsByParentAndDeletedAtIsNull(category)) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Cannot delete category with children");
        }
        if (songRepository.existsByCategoryAndDeletedAtIsNull(category)) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Cannot delete category with songs");
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public SongCategory updateVisibility(Long id, boolean isVisible) {
        SongCategory category = getById(id);
        category.setIsVisible(isVisible);
        if (!isVisible) {
            List<SongCategory> children = categoryRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("parent"), category),
                cb.isNull(root.get("deletedAt"))
            ));
            for (SongCategory child : children) {
                child.setIsVisible(false);
            }
            songRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("category"), category),
                cb.isNull(root.get("deletedAt"))
            ))
                .forEach(song -> song.setIsVisible(false));
            for (SongCategory child : children) {
                songRepository.findAll((root, query, cb) -> cb.and(
                    cb.equal(root.get("category"), child),
                    cb.isNull(root.get("deletedAt"))
                ))
                    .forEach(song -> song.setIsVisible(false));
            }
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void reorder(ReorderSongCategoryRequest request) {
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Orders cannot be empty");
        }
        SongCategory parent = null;
        if (request.getParentId() != null) {
            parent = getById(request.getParentId());
        }
        List<Long> ids = request.getOrders().stream().map(ReorderSongCategoryRequest.OrderItem::getId).toList();
        List<SongCategory> categories = categoryRepository.findAllById(ids);
        if (categories.size() != ids.size()) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some categories not found");
        }
        for (SongCategory category : categories) {
            if (category.getDeletedAt() != null) {
                throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some categories not found");
            }
            if (parent == null) {
                if (category.getParent() != null) {
                    throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Category parent mismatch in reorder scope");
                }
            } else if (category.getParent() == null || !category.getParent().getId().equals(parent.getId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Category parent mismatch in reorder scope");
            }
        }
        for (ReorderSongCategoryRequest.OrderItem order : request.getOrders()) {
            categories.stream()
                .filter(c -> c.getId().equals(order.getId()))
                .findFirst()
                .ifPresent(c -> c.setSortOrder(order.getSortOrder()));
        }
        categoryRepository.saveAll(categories);
    }

    private <T> T valueOrDefault(T requestedValue, T currentValue) {
        return requestedValue != null ? requestedValue : currentValue;
    }
}
