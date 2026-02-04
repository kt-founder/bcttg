package com.bcttg.module.media.repository;

import com.bcttg.module.media.entity.MediaAsset;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
}
