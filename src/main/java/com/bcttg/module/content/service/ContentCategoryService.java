package com.bcttg.module.content.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.content.dto.CreateContentCategoryRequest;
import com.bcttg.module.content.dto.ReorderContentCategoryRequest;
import com.bcttg.module.content.dto.UpdateContentCategoryRequest;
import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.content.repository.ContentCategoryRepository;
import com.bcttg.module.content.repository.ContentItemRepository;
import com.bcttg.module.dashboard.service.SystemAuditTrailService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentCategoryService {
    private final ContentCategoryRepository categoryRepository;
    private final ContentItemRepository itemRepository;
    private final SystemAuditTrailService auditTrailService;

    public ContentCategoryService(
        ContentCategoryRepository categoryRepository,
        ContentItemRepository itemRepository,
        SystemAuditTrailService auditTrailService
    ) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.auditTrailService = auditTrailService;
    }

    public Page<ContentCategory> findAll(ContentType type, Long parentId, String q, Boolean isVisible, Pageable pageable) {
        Specification<ContentCategory> spec = Specification.<ContentCategory>where(
            (root, query, cb) -> cb.isNull(root.get("deletedAt"))
        );
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }
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

    public Page<ContentCategory> findAllPublic(ContentType type, Long parentId, String q, Pageable pageable) {
        return findAll(type, parentId, q, true, pageable);
    }

    public ContentCategory getById(Long id) {
        ContentCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content category not found"));
        if (category.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content category not found");
        }
        return category;
    }

    public ContentCategory getVisibleById(Long id) {
        ContentCategory category = getById(id);
        if (!Boolean.TRUE.equals(category.getIsVisible())) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content category not found");
        }
        return category;
    }

    @Transactional
    public ContentCategory create(CreateContentCategoryRequest request, String actorPhone) {
        ContentCategory parent = null;
        if (request.getParentId() != null) {
            parent = getById(request.getParentId());
            if (parent.getParent() != null) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Only 2 levels of content categories are allowed");
            }
            if (parent.getType() != request.getType()) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Parent type must match category type");
            }
        }

        if (categoryRepository.existsByTypeAndParentAndSlugAndDeletedAtIsNull(request.getType(), parent, request.getSlug())) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Slug already exists in scope");
        }

        ContentCategory category = new ContentCategory();
        category.setType(request.getType());
        category.setParent(parent);
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setIsVisible(Optional.ofNullable(request.getIsVisible()).orElse(true));
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            sortOrder = categoryRepository.findMaxSortOrder(request.getType(), parent) + 1;
        }
        category.setSortOrder(sortOrder);
        ContentCategory saved = categoryRepository.save(category);
        auditTrailService.record(actorPhone, "CREATE", "CONTENT_CATEGORY", saved.getName(), "tạo danh mục nội dung “" + saved.getName() + "”");
        return saved;
    }

    @Transactional
    public ContentCategory update(Long id, UpdateContentCategoryRequest request, String actorPhone) {
        ContentCategory category = getById(id);

        ContentCategory parent = category.getParent();
        if (request.getParentId() != null) {
            parent = getById(request.getParentId());
            if (parent.getParent() != null) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Only 2 levels of content categories are allowed");
            }
            if (parent.getType() != category.getType()) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Parent type must match category type");
            }
            if (parent.getId().equals(category.getId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Parent cannot be itself");
            }
        }

        String slug = valueOrDefault(request.getSlug(), category.getSlug());
        ContentCategory existing = categoryRepository.findByTypeAndParentAndSlug(category.getType(), parent, slug).orElse(null);
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
        ContentCategory saved = categoryRepository.save(category);
        auditTrailService.record(actorPhone, "UPDATE", "CONTENT_CATEGORY", saved.getName(), "chỉnh sửa danh mục nội dung “" + saved.getName() + "”");
        return saved;
    }

    @Transactional
    public void delete(Long id, String actorPhone) {
        ContentCategory category = getById(id);
        if (categoryRepository.existsByParentAndDeletedAtIsNull(category)) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Cannot delete category with children");
        }
        if (itemRepository.existsByCategoryAndDeletedAtIsNull(category)) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Cannot delete category with content items");
        }
        String name = category.getName();
        categoryRepository.delete(category);
        auditTrailService.record(actorPhone, "DELETE", "CONTENT_CATEGORY", name, "xóa danh mục nội dung “" + name + "”");
    }

    @Transactional
    public ContentCategory updateVisibility(Long id, boolean isVisible, String actorPhone) {
        ContentCategory category = getById(id);
        category.setIsVisible(isVisible);

        if (!isVisible) {
            List<ContentCategory> children = categoryRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("parent"), category),
                cb.isNull(root.get("deletedAt"))
            ));
            for (ContentCategory child : children) {
                child.setIsVisible(false);
                itemRepository.findAll((root, query, cb) -> cb.and(
                    cb.equal(root.get("category"), child),
                    cb.isNull(root.get("deletedAt"))
                ))
                    .forEach(item -> item.setIsVisible(false));
            }
            itemRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("category"), category),
                cb.isNull(root.get("deletedAt"))
            ))
                .forEach(item -> item.setIsVisible(false));
        }

        ContentCategory saved = categoryRepository.save(category);
        String phrase = isVisible ? "hiển thị danh mục nội dung “" + saved.getName() + "”" : "ẩn danh mục nội dung “" + saved.getName() + "”";
        auditTrailService.record(actorPhone, "UPDATE", "CONTENT_CATEGORY", saved.getName(), phrase);
        return saved;
    }

    @Transactional
    public void reorder(ReorderContentCategoryRequest request, String actorPhone) {
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Orders cannot be empty");
        }
        ContentCategory parent = null;
        if (request.getParentId() != null) {
            parent = getById(request.getParentId());
        }
        List<Long> ids = request.getOrders().stream().map(ReorderContentCategoryRequest.OrderItem::getId).toList();
        List<ContentCategory> categories = categoryRepository.findAllById(ids);
        if (categories.size() != ids.size()) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some categories not found");
        }
        for (ContentCategory category : categories) {
            if (category.getDeletedAt() != null) {
                throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some categories not found");
            }
            if (category.getType() != request.getType()) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Category type mismatch in reorder scope");
            }
            if (parent == null) {
                if (category.getParent() != null) {
                    throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Category parent mismatch in reorder scope");
                }
            } else if (category.getParent() == null || !category.getParent().getId().equals(parent.getId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Category parent mismatch in reorder scope");
            }
        }
        for (ReorderContentCategoryRequest.OrderItem order : request.getOrders()) {
            categories.stream()
                .filter(c -> c.getId().equals(order.getId()))
                .findFirst()
                .ifPresent(c -> c.setSortOrder(order.getSortOrder()));
        }
        categoryRepository.saveAll(categories);
        String scopeName = parent == null ? request.getType().name() : parent.getName();
        auditTrailService.record(actorPhone, "UPDATE", "CONTENT_CATEGORY", scopeName, "sắp xếp lại danh mục nội dung");
    }

    private <T> T valueOrDefault(T requestedValue, T currentValue) {
        return requestedValue != null ? requestedValue : currentValue;
    }
}
