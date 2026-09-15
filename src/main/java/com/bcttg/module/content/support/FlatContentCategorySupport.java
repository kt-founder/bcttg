package com.bcttg.module.content.support;

import java.util.Map;
import java.util.Optional;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.content.repository.ContentCategoryRepository;
import com.bcttg.module.dashboard.service.SystemAuditTrailService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FlatContentCategorySupport {
    public static final String NET_TIEU_BIEU_ROOT_SLUG = "net-tieu-bieu";
    public static final String NET_TIEU_BIEU_ROOT_NAME = "Nét tiêu biểu";
    public static final String NET_TIEU_BIEU_ROOT_DESCRIPTION = "Danh mục gốc module Nét tiêu biểu";

    public static final String TIN_TUC_ROOT_SLUG = "tin-tuc";
    public static final String TIN_TUC_ROOT_NAME = "Tin tức";
    public static final String TIN_TUC_ROOT_DESCRIPTION = "Danh mục gốc module Tin tức";

    private record FlatRootSpec(String slug, String name, String description, String auditNote) {
    }

    private static final Map<ContentType, FlatRootSpec> FLAT_ROOTS = Map.of(
        ContentType.NET_TIEU_BIEU, new FlatRootSpec(
            NET_TIEU_BIEU_ROOT_SLUG,
            NET_TIEU_BIEU_ROOT_NAME,
            NET_TIEU_BIEU_ROOT_DESCRIPTION,
            "tự động tạo danh mục gốc Nét tiêu biểu"
        ),
        ContentType.TIN_TUC, new FlatRootSpec(
            TIN_TUC_ROOT_SLUG,
            TIN_TUC_ROOT_NAME,
            TIN_TUC_ROOT_DESCRIPTION,
            "tự động tạo danh mục gốc Tin tức"
        )
    );

    private final ContentCategoryRepository categoryRepository;
    private final SystemAuditTrailService auditTrailService;

    public FlatContentCategorySupport(
        ContentCategoryRepository categoryRepository,
        SystemAuditTrailService auditTrailService
    ) {
        this.categoryRepository = categoryRepository;
        this.auditTrailService = auditTrailService;
    }

    public static boolean isFlatType(ContentType type) {
        return type != null && FLAT_ROOTS.containsKey(type);
    }

    @Transactional
    public ContentCategory resolveNetTieuBieuRootCategory(String actorPhone) {
        return resolveFlatRootCategory(ContentType.NET_TIEU_BIEU, actorPhone);
    }

    @Transactional
    public ContentCategory resolveFlatRootCategory(ContentType type, String actorPhone) {
        FlatRootSpec spec = FLAT_ROOTS.get(type);
        if (spec == null) {
            throw new ApiException(
                ErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST,
                "Content type does not support flat mode: " + type
            );
        }

        Optional<ContentCategory> existing = categoryRepository
            .findByTypeAndParentAndSlug(type, null, spec.slug())
            .filter(category -> category.getDeletedAt() == null);
        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            ContentCategory category = new ContentCategory();
            category.setType(type);
            category.setParent(null);
            category.setName(spec.name());
            category.setSlug(spec.slug());
            category.setDescription(spec.description());
            category.setIsVisible(true);
            category.setSortOrder(categoryRepository.findMaxSortOrder(type, null) + 1);
            ContentCategory saved = categoryRepository.save(category);
            auditTrailService.record(
                actorPhone,
                "CREATE",
                "CONTENT_CATEGORY",
                saved.getName(),
                spec.auditNote()
            );
            return saved;
        } catch (DataIntegrityViolationException ex) {
            return categoryRepository
                .findByTypeAndParentAndSlug(type, null, spec.slug())
                .filter(category -> category.getDeletedAt() == null)
                .orElseThrow(() -> new ApiException(
                    ErrorCode.CONFLICT,
                    HttpStatus.CONFLICT,
                    "Không thể tạo danh mục gốc " + spec.name()
                ));
        }
    }
}
