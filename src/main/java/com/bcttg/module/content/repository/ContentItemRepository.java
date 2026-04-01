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

    @Query(value = "SELECT MONTH(created_at) AS month_no, COUNT(*) AS total " +
        "FROM content_items " +
        "WHERE deleted_at IS NULL AND YEAR(created_at) = :year " +
        "GROUP BY MONTH(created_at)", nativeQuery = true)
    java.util.List<Object[]> countCreatedByMonth(@Param("year") int year);

    @Query("select c.type, count(i) from ContentItem i join i.category c where i.deletedAt is null group by c.type")
    java.util.List<Object[]> countByCategoryType();

    @Query("select count(i) from ContentItem i join i.category c where i.deletedAt is null and i.isVisible = true and c.type = :type")
    long countVisibleByCategoryType(@Param("type") com.bcttg.module.content.entity.ContentType type);

    @EntityGraph(attributePaths = {"coverMedia", "category"})
    java.util.List<ContentItem> findTop5ByDeletedAtIsNullAndIsVisibleFalseOrderByUpdatedAtDesc();

    @Query("select coalesce(sum(i.viewCount), 0) from ContentItem i where i.deletedAt is null")
    long sumViewCount();

    @EntityGraph(attributePaths = {"coverMedia", "category"})
    java.util.List<ContentItem> findTop10ByDeletedAtIsNullOrderByViewCountDescUpdatedAtDesc();
}
