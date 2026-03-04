package com.bcttg.module.profile.repository;

import java.util.List;
import java.util.Optional;

import com.bcttg.module.profile.entity.DataProfile;
import com.bcttg.module.profile.entity.ProfileType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DataProfileRepository extends JpaRepository<DataProfile, Long>, JpaSpecificationExecutor<DataProfile> {
    @EntityGraph(attributePaths = {"avatarMedia"})
    Page<DataProfile> findAll(Specification<DataProfile> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"avatarMedia"})
    Optional<DataProfile> findById(Long id);

    @Query("select coalesce(max(p.sortOrder), 0) from DataProfile p where p.deletedAt is null and p.profileType = :profileType")
    Integer findMaxSortOrder(@Param("profileType") ProfileType profileType);

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndProfileType(ProfileType profileType);

    @EntityGraph(attributePaths = {"avatarMedia"})
    List<DataProfile> findTop5ByDeletedAtIsNullAndIsVisibleFalseOrderByUpdatedAtDesc();
}
