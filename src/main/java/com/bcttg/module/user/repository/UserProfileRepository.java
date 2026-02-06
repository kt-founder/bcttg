package com.bcttg.module.user.repository;

import com.bcttg.module.user.entity.UserProfile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
