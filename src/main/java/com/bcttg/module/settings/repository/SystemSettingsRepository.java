package com.bcttg.module.settings.repository;

import java.util.Optional;

import com.bcttg.module.settings.entity.SystemSettings;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
    Optional<SystemSettings> findTopByDeletedAtIsNullOrderByIdAsc();
}
