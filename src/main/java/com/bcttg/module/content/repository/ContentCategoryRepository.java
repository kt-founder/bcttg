package com.bcttg.module.content.repository;

import java.util.Optional;

import com.bcttg.module.content.entity.ContentCategory;
import com.bcttg.module.content.entity.ContentType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, Long>, JpaSpecificationExecutor<ContentCategory> {
    boolean existsByTypeAndParentAndSlug(ContentType type, ContentCategory parent, String slug);

    boolean existsByTypeAndParentAndSlugAndDeletedAtIsNull(ContentType type, ContentCategory parent, String slug);

    Optional<ContentCategory> findByTypeAndParentAndSlug(ContentType type, ContentCategory parent, String slug);

    boolean existsByParent(ContentCategory parent);

    boolean existsByParentAndDeletedAtIsNull(ContentCategory parent);

    @Query("select coalesce(max(c.sortOrder), 0) from ContentCategory c where c.deletedAt is null and c.type = :type and ((:parent is null and c.parent is null) or c.parent = :parent)")
    Integer findMaxSortOrder(@Param("type") ContentType type, @Param("parent") ContentCategory parent);

    Optional<ContentCategory> findByIdAndIsVisibleTrue(Long id);
}
