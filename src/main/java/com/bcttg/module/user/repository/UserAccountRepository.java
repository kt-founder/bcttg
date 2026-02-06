package com.bcttg.module.user.repository;

import java.util.Optional;

import com.bcttg.module.user.entity.UserAccount;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByPhoneAndDeletedAtIsNull(String phone);
}
