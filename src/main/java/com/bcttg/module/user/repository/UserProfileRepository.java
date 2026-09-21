package com.bcttg.module.user.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.bcttg.module.user.entity.UserProfile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserIdAndDeletedAtIsNull(Long userId);

    List<UserProfile> findAllByUserIdInAndDeletedAtIsNull(Collection<Long> userIds);
}
