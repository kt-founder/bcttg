package com.bcttg.module.dashboard.service;

import java.util.Optional;

import com.bcttg.module.dashboard.entity.SystemAuditLog;
import com.bcttg.module.dashboard.repository.SystemAuditLogRepository;
import com.bcttg.module.user.entity.UserAccount;
import com.bcttg.module.user.entity.UserProfile;
import com.bcttg.module.user.repository.UserAccountRepository;
import com.bcttg.module.user.repository.UserProfileRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemAuditTrailService {
    private final SystemAuditLogRepository repository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;

    public SystemAuditTrailService(
        SystemAuditLogRepository repository,
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository
    ) {
        this.repository = repository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public void record(String actorPhone, String actionType, String moduleName, String entityName, String detail) {
        record(actorPhone, actionType, moduleName, entityName, detail, "SUCCESS");
    }

    @Transactional
    public void record(String actorPhone, String actionType, String moduleName, String entityName, String detail, String status) {
        UserAccount actorUser = resolveActorUser(actorPhone);
        String actorName = resolveActorName(actorUser, actorPhone);

        SystemAuditLog log = new SystemAuditLog();
        log.setActorUser(actorUser);
        log.setActorName(actorName);
        log.setActionType(actionType);
        log.setModuleName(moduleName);
        log.setEntityName(entityName);
        log.setDetail(detail);
        log.setStatus(status);
        repository.save(log);
    }

    private UserAccount resolveActorUser(String actorPhone) {
        if (actorPhone == null || actorPhone.isBlank()) {
            return null;
        }
        return userAccountRepository.findByPhoneAndDeletedAtIsNull(actorPhone).orElse(null);
    }

    private String resolveActorName(UserAccount actorUser, String actorPhone) {
        if (actorUser == null) {
            return actorPhone != null && !actorPhone.isBlank() ? actorPhone : "System";
        }
        Optional<UserProfile> profile = userProfileRepository.findByUserIdAndDeletedAtIsNull(actorUser.getId());
        return profile.map(UserProfile::getFullName).orElse(actorUser.getPhone());
    }
}
