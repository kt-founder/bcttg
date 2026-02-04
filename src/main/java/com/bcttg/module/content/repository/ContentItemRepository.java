package com.bcttg.module.content.repository;

import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentItem;
import com.bcttg.module.media.entity.MediaAsset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentItemRepository extends JpaRepository<ContentItem, Long>, JpaSpecificationExecutor<ContentItem> {
    boolean existsByCategory(ContentCategory category);

    boolean existsByCategoryAndDeletedAtIsNull(ContentCategory category);

    boolean existsByCoverMedia(MediaAsset coverMedia);

    boolean existsByCoverMediaAndDeletedAtIsNull(MediaAsset coverMedia);

    @Query("select coalesce(max(c.sortOrder), 0) from ContentItem c where c.deletedAt is null and c.category = :category")
    Integer findMaxSortOrder(@Param("category") ContentCategory category);

    ContentItem findByIdAndIsVisibleTrue(Long id);
}
