package com.bcttg.module.content.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.content.dto.CreateContentItemRequest;
import com.bcttg.module.content.dto.ReorderContentItemRequest;
import com.bcttg.module.content.dto.UpdateContentItemRequest;
import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentItem;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.content.repository.ContentCategoryRepository;
import com.bcttg.module.content.repository.ContentItemRepository;
import com.bcttg.module.dashboard.service.SystemAuditTrailService;
import com.bcttg.module.home.service.PublicModuleAccessService;
import com.bcttg.module.media.entity.MediaAsset;
import com.bcttg.module.media.repository.MediaAssetRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentItemService {
    private final ContentCategoryRepository categoryRepository;
    private final ContentItemRepository itemRepository;
    private final MediaAssetRepository mediaRepository;
    private final SystemAuditTrailService auditTrailService;
    private final PublicModuleAccessService publicModuleAccessService;

    public ContentItemService(
        ContentCategoryRepository categoryRepository,
        ContentItemRepository itemRepository,
        MediaAssetRepository mediaRepository,
        SystemAuditTrailService auditTrailService,
        PublicModuleAccessService publicModuleAccessService
    ) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.mediaRepository = mediaRepository;
        this.auditTrailService = auditTrailService;
        this.publicModuleAccessService = publicModuleAccessService;
    }

    public Page<ContentItem> findAll(Long categoryId, ContentType type, String q, Boolean isVisible, Pageable pageable) {
        Specification<ContentItem> spec = Specification.<ContentItem>where(
            (root, query, cb) -> cb.isNull(root.get("deletedAt"))
        );
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("type"), type));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("summary")), like)
            ));
        }
        if (isVisible != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isVisible"), isVisible));
        }
        return itemRepository.findAll(spec, pageable);
    }

    public Page<ContentItem> findAllPublic(Long categoryId, ContentType type, String q, Pageable pageable, Authentication authentication) {
        if (publicModuleAccessService.isAuthenticated(authentication)) {
            return findAll(categoryId, type, q, true, pageable);
        }
        if (categoryId != null) {
            ContentCategory category = getVisibleCategory(categoryId);
            publicModuleAccessService.ensureContentAccess(authentication, category.getType());
            return findAll(categoryId, type, q, true, pageable);
        }
        if (type != null) {
            publicModuleAccessService.ensureContentAccess(authentication, type);
            return findAll(categoryId, type, q, true, pageable);
        }
        Set<ContentType> guestTypes = publicModuleAccessService.getGuestContentTypes();
        if (guestTypes.isEmpty()) {
            return Page.empty(pageable);
        }
        Specification<ContentItem> spec = buildSpecification(categoryId, type, q, true)
            .and((root, query, cb) -> root.get("category").get("type").in(guestTypes));
        return itemRepository.findAll(spec, pageable);
    }

    public ContentItem getById(Long id) {
        ContentItem item = itemRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content item not found"));
        if (item.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content item not found");
        }
        return item;
    }

    public ContentItem getVisibleById(Long id) {
        ContentItem item = getById(id);
        if (!Boolean.TRUE.equals(item.getIsVisible())) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content item not found");
        }
        return item;
    }

    public ContentItem getVisibleById(Long id, Authentication authentication) {
        ContentItem item = getVisibleById(id);
        publicModuleAccessService.ensureContentAccess(authentication, item.getCategory().getType());
        return item;
    }

    @Transactional
    public ContentItem create(CreateContentItemRequest request, String actorPhone) {
        ContentCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content category not found"));

        MediaAsset coverMedia = null;
        if (request.getCoverMediaId() != null) {
            coverMedia = mediaRepository.findById(request.getCoverMediaId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Cover media not found"));
        }

        ContentItem item = new ContentItem();
        item.setCategory(category);
        item.setTitle(request.getTitle());
        item.setSummary(request.getSummary());
        item.setBodyHtml(request.getBodyHtml());
        item.setCoverMedia(coverMedia);
        item.setIsVisible(Optional.ofNullable(request.getIsVisible()).orElse(true));
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            sortOrder = itemRepository.findMaxSortOrder(category) + 1;
        }
        item.setSortOrder(sortOrder);
        item.setPublishedAt(request.getPublishedAt());
        ContentItem saved = itemRepository.save(item);
        auditTrailService.record(actorPhone, "CREATE", "CONTENT", saved.getTitle(), "tạo nội dung “" + saved.getTitle() + "”");
        return saved;
    }

    @Transactional
    public ContentItem update(Long id, UpdateContentItemRequest request, String actorPhone) {
        ContentItem item = getById(id);
        ContentCategory category = item.getCategory();
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content category not found"));
        }
        MediaAsset coverMedia = item.getCoverMedia();
        if (request.getCoverMediaId() != null) {
            coverMedia = mediaRepository.findById(request.getCoverMediaId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Cover media not found"));
        }
        item.setCategory(category);
        item.setTitle(valueOrDefault(request.getTitle(), item.getTitle()));
        if (request.getSummary() != null) {
            item.setSummary(request.getSummary());
        }
        item.setBodyHtml(valueOrDefault(request.getBodyHtml(), item.getBodyHtml()));
        item.setCoverMedia(coverMedia);
        if (request.getIsVisible() != null) {
            item.setIsVisible(request.getIsVisible());
        }
        if (request.getSortOrder() != null) {
            item.setSortOrder(request.getSortOrder());
        }
        if (request.getPublishedAt() != null) {
            item.setPublishedAt(request.getPublishedAt());
        }
        ContentItem saved = itemRepository.save(item);
        auditTrailService.record(actorPhone, "UPDATE", "CONTENT", saved.getTitle(), "chỉnh sửa nội dung “" + saved.getTitle() + "”");
        return saved;
    }

    @Transactional
    public void delete(Long id, String actorPhone) {
        ContentItem item = getById(id);
        String title = item.getTitle();
        itemRepository.delete(item);
        auditTrailService.record(actorPhone, "DELETE", "CONTENT", title, "xóa nội dung “" + title + "”");
    }

    @Transactional
    public ContentItem updateVisibility(Long id, boolean isVisible, String actorPhone) {
        ContentItem item = getById(id);
        item.setIsVisible(isVisible);
        ContentItem saved = itemRepository.save(item);
        String phrase = isVisible ? "hiển thị nội dung “" + saved.getTitle() + "”" : "ẩn nội dung “" + saved.getTitle() + "”";
        auditTrailService.record(actorPhone, "UPDATE", "CONTENT", saved.getTitle(), phrase);
        return saved;
    }

    @Transactional
    public void reorder(ReorderContentItemRequest request, String actorPhone) {
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Orders cannot be empty");
        }
        ContentCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content category not found"));
        List<Long> ids = request.getOrders().stream().map(ReorderContentItemRequest.OrderItem::getId).toList();
        List<ContentItem> items = itemRepository.findAllById(ids);
        if (items.size() != ids.size()) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some content items not found");
        }
        for (ContentItem item : items) {
            if (item.getDeletedAt() != null) {
                throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some content items not found");
            }
            if (!item.getCategory().getId().equals(category.getId())) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Item category mismatch in reorder scope");
            }
        }
        for (ReorderContentItemRequest.OrderItem order : request.getOrders()) {
            items.stream()
                .filter(i -> i.getId().equals(order.getId()))
                .findFirst()
                .ifPresent(i -> i.setSortOrder(order.getSortOrder()));
        }
        itemRepository.saveAll(items);
        auditTrailService.record(actorPhone, "UPDATE", "CONTENT", category.getName(), "sắp xếp lại nội dung của danh mục “" + category.getName() + "”");
    }

    @Transactional
    public ContentItem incrementViewCount(Long id, String actorPhone, Authentication authentication) {
        ContentItem item = getVisibleById(id, authentication);
        int currentViewCount = item.getViewCount() != null ? item.getViewCount() : 0;
        item.setViewCount(currentViewCount + 1);
        ContentItem saved = itemRepository.save(item);
        String auditActor = publicModuleAccessService.isAuthenticated(authentication) ? actorPhone : null;
        auditTrailService.record(auditActor, "VIEW", "CONTENT", saved.getTitle(), "content_id=" + saved.getId());
        return saved;
    }

    private Specification<ContentItem> buildSpecification(Long categoryId, ContentType type, String q, Boolean isVisible) {
        Specification<ContentItem> spec = Specification.<ContentItem>where(
            (root, query, cb) -> cb.isNull(root.get("deletedAt"))
        );
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("type"), type));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("summary")), like)
            ));
        }
        if (isVisible != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isVisible"), isVisible));
        }
        return spec;
    }

    private ContentCategory getVisibleCategory(Long categoryId) {
        ContentCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content category not found"));
        if (category.getDeletedAt() != null || !Boolean.TRUE.equals(category.getIsVisible())) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Content category not found");
        }
        return category;
    }

    private <T> T valueOrDefault(T requestedValue, T currentValue) {
        return requestedValue != null ? requestedValue : currentValue;
    }
}
