package com.bcttg.module.home.repository;

import java.util.List;

import com.bcttg.module.home.entity.HomeModuleConfig;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeModuleRepository extends JpaRepository<HomeModuleConfig, String> {
    List<HomeModuleConfig> findAllByOrderBySortOrderAscIdAsc();

    List<HomeModuleConfig> findAllByEnabledTrueOrderBySortOrderAscIdAsc();

    List<HomeModuleConfig> findAllByEnabledTrueAndIsGuestTrueOrderBySortOrderAscIdAsc();
}
