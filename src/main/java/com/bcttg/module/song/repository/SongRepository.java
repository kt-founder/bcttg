package com.bcttg.module.song.repository;

import com.bcttg.module.media.entity.MediaAsset;
import com.bcttg.module.song.entity.Song;
import com.bcttg.module.song.entity.SongCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {
    @Query("select coalesce(max(s.sortOrder), 0) from Song s where s.deletedAt is null and ((:category is null and s.category is null) or s.category = :category)")
    Integer findMaxSortOrder(@Param("category") SongCategory category);

    Song findByIdAndIsVisibleTrue(Long id);

    boolean existsByCategoryAndDeletedAtIsNull(SongCategory category);

    boolean existsByAudioMediaAndDeletedAtIsNull(MediaAsset audioMedia);
}
