package com.bcttg.module.song.repository;

import com.bcttg.module.song.entity.SongCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SongCategoryRepository extends JpaRepository<SongCategory, Long>, JpaSpecificationExecutor<SongCategory> {
    boolean existsByParent(SongCategory parent);

    boolean existsByParentAndDeletedAtIsNull(SongCategory parent);

    boolean existsByParentAndSlug(SongCategory parent, String slug);

    boolean existsByParentAndSlugAndDeletedAtIsNull(SongCategory parent, String slug);

    Optional<SongCategory> findByParentAndSlug(SongCategory parent, String slug);

    @Query("select coalesce(max(s.sortOrder), 0) from SongCategory s where s.deletedAt is null and ((:parent is null and s.parent is null) or s.parent = :parent)")
    Integer findMaxSortOrder(@Param("parent") SongCategory parent);

    SongCategory findByIdAndIsVisibleTrue(Long id);
}
