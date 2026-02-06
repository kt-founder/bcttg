package com.bcttg.module.content.repository;

import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentItem;
import com.bcttg.module.media.entity.MediaAsset;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentItemRepository extends JpaRepository<ContentItem, Long>, JpaSpecificationExecutor<ContentItem> {
    @EntityGraph(attributePaths = {"coverMedia", "category"})
    Page<ContentItem> findAll(Specification<ContentItem> spec, Pageable pageable);

    boolean existsByCategory(ContentCategory category);

    boolean existsByCategoryAndDeletedAtIsNull(ContentCategory category);

    boolean existsByCoverMedia(MediaAsset coverMedia);

    boolean existsByCoverMediaAndDeletedAtIsNull(MediaAsset coverMedia);

    @Query("select coalesce(max(c.sortOrder), 0) from ContentItem c where c.deletedAt is null and c.category = :category")
    Integer findMaxSortOrder(@Param("category") ContentCategory category);

    @EntityGraph(attributePaths = {"coverMedia", "category"})
    Optional<ContentItem> findById(Long id);

    @EntityGraph(attributePaths = {"coverMedia", "category"})
    ContentItem findByIdAndIsVisibleTrue(Long id);
}
