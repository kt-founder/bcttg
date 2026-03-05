package com.bcttg.module.user.repository;

import java.util.Optional;

import com.bcttg.module.user.entity.UserAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long>, JpaSpecificationExecutor<UserAccount> {
    Optional<UserAccount> findByPhoneAndDeletedAtIsNull(String phone);

    Optional<UserAccount> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    boolean existsByPhoneAndDeletedAtIsNullAndIdNot(String phone, Long id);

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndIsActiveTrue();
}
