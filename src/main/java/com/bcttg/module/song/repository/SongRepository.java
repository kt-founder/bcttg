package com.bcttg.module.song.repository;

import com.bcttg.module.media.entity.MediaAsset;
import com.bcttg.module.song.entity.Song;
import com.bcttg.module.song.entity.SongCategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {
    @EntityGraph(attributePaths = {"audioMedia", "category"})
    Page<Song> findAll(Specification<Song> spec, Pageable pageable);

    @Query("select coalesce(max(s.sortOrder), 0) from Song s where s.deletedAt is null and ((:category is null and s.category is null) or s.category = :category)")
    Integer findMaxSortOrder(@Param("category") SongCategory category);

    @EntityGraph(attributePaths = {"audioMedia", "category"})
    Optional<Song> findById(Long id);

    @EntityGraph(attributePaths = {"audioMedia", "category"})
    Song findByIdAndIsVisibleTrue(Long id);

    boolean existsByCategoryAndDeletedAtIsNull(SongCategory category);

    boolean existsByAudioMediaAndDeletedAtIsNull(MediaAsset audioMedia);

    long countByDeletedAtIsNullAndIsVisibleTrue();

    @EntityGraph(attributePaths = {"audioMedia", "category"})
    java.util.List<Song> findTop5ByDeletedAtIsNullAndIsVisibleFalseOrderByUpdatedAtDesc();
}
