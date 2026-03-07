package com.bcttg.module.user.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.user.dto.CreateUserRequest;
import com.bcttg.module.user.dto.UpdateUserRequest;
import com.bcttg.module.user.dto.UpdateUserProfilePayload;
import com.bcttg.module.user.dto.UserAdminResponse;
import com.bcttg.module.user.dto.UserProfilePayload;
import com.bcttg.module.user.entity.UserAccount;
import com.bcttg.module.user.entity.UserProfile;
import com.bcttg.module.user.entity.UserRole;
import com.bcttg.module.user.repository.UserAccountRepository;
import com.bcttg.module.user.repository.UserProfileRepository;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {
    private static final Pattern PASSWORD_UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile(".*[0-9].*");

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserAdminResponse> findAll(String q, UserRole role, Boolean isActive, Pageable pageable) {
        Specification<UserAccount> spec = Specification.where(notDeleted());
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(searchByKeyword(q));
        }

        Page<UserAccount> page = userAccountRepository.findAll(spec, pageable);
        Map<Long, UserProfile> profileByUserId = loadProfiles(page.stream().map(UserAccount::getId).toList());
        return page.map(account -> new UserAdminResponse(account, profileByUserId.get(account.getId())));
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getById(Long id) {
        UserAccount account = getAccountById(id);
        UserProfile profile = userProfileRepository.findByUserIdAndDeletedAtIsNull(id).orElse(null);
        return new UserAdminResponse(account, profile);
    }

    @Transactional
    public UserAdminResponse create(CreateUserRequest request) {
        if (userAccountRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Phone already exists");
        }
        validatePasswordStrength(request.getPassword());

        UserAccount account = new UserAccount();
        account.setPhone(request.getPhone());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setRole(request.getRole());
        account.setIsActive(request.getIsActive() == null || request.getIsActive());
        account = userAccountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUser(account);
        applyProfile(profile, request.getProfile());
        profile = userProfileRepository.save(profile);

        return new UserAdminResponse(account, profile);
    }

    @Transactional
    public UserAdminResponse update(Long id, UpdateUserRequest request, String actorPhone) {
        UserAccount account = getAccountById(id);
        String phone = valueOrDefault(request.getPhone(), account.getPhone());
        UserRole role = valueOrDefault(request.getRole(), account.getRole());
        boolean isActive = valueOrDefault(request.getIsActive(), account.getIsActive());

        if (!account.getPhone().equals(phone)
            && userAccountRepository.existsByPhoneAndDeletedAtIsNullAndIdNot(phone, id)) {
            throw new ApiException(ErrorCode.CONFLICT, HttpStatus.CONFLICT, "Phone already exists");
        }
        if (isSelf(account, actorPhone) && account.getRole() == UserRole.ADMIN && role != UserRole.ADMIN) {
            throw new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Cannot downgrade your own admin role");
        }
        if (isSelf(account, actorPhone) && !isActive) {
            throw new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Cannot deactivate your own account");
        }

        account.setPhone(phone);
        account.setRole(role);
        account.setIsActive(isActive);
        account = userAccountRepository.save(account);
        UserAccount savedAccount = account;

        Optional<UserProfile> existingProfile = userProfileRepository.findByUserIdAndDeletedAtIsNull(id);
        UserProfile profile = existingProfile.orElse(null);
        if (request.getProfile() != null) {
            if (profile == null) {
                profile = new UserProfile();
                profile.setUser(savedAccount);
            }
            applyProfilePatch(profile, request.getProfile());
            if (profile.getFullName() == null || profile.getFullName().isBlank()) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Profile fullName is required");
            }
            profile = userProfileRepository.save(profile);
        }

        return new UserAdminResponse(account, profile);
    }

    @Transactional
    public UserAdminResponse updateActive(Long id, boolean value, String actorPhone) {
        UserAccount account = getAccountById(id);
        if (isSelf(account, actorPhone) && !value) {
            throw new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Cannot deactivate your own account");
        }
        account.setIsActive(value);
        account = userAccountRepository.save(account);
        UserProfile profile = userProfileRepository.findByUserIdAndDeletedAtIsNull(id).orElse(null);
        return new UserAdminResponse(account, profile);
    }

    @Transactional
    public UserAdminResponse updateRole(Long id, UserRole role, String actorPhone) {
        UserAccount account = getAccountById(id);
        if (isSelf(account, actorPhone) && account.getRole() == UserRole.ADMIN && role != UserRole.ADMIN) {
            throw new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Cannot downgrade your own admin role");
        }
        account.setRole(role);
        account = userAccountRepository.save(account);
        UserProfile profile = userProfileRepository.findByUserIdAndDeletedAtIsNull(id).orElse(null);
        return new UserAdminResponse(account, profile);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        validatePasswordStrength(newPassword);
        UserAccount account = getAccountById(id);
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(account);
    }

    @Transactional
    public void delete(Long id, String actorPhone) {
        UserAccount account = getAccountById(id);
        if (isSelf(account, actorPhone)) {
            throw new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Cannot delete your own account");
        }
        userProfileRepository.findByUserIdAndDeletedAtIsNull(id).ifPresent(userProfileRepository::delete);
        userAccountRepository.delete(account);
    }

    private Specification<UserAccount> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private Specification<UserAccount> searchByKeyword(String q) {
        return (root, query, cb) -> {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            Predicate phoneMatches = cb.like(cb.lower(root.get("phone")), like);

            var subquery = query.subquery(Long.class);
            var profileRoot = subquery.from(UserProfile.class);
            subquery.select(profileRoot.get("id"));

            Predicate profileOfUser = cb.equal(profileRoot.get("user").get("id"), root.get("id"));
            Predicate profileNotDeleted = cb.isNull(profileRoot.get("deletedAt"));
            Predicate profileMatches = cb.or(
                cb.like(cb.lower(profileRoot.get("fullName")), like),
                cb.like(cb.lower(profileRoot.get("email")), like),
                cb.like(cb.lower(profileRoot.get("unitName")), like)
            );
            subquery.where(cb.and(profileOfUser, profileNotDeleted, profileMatches));

            return cb.or(phoneMatches, cb.exists(subquery));
        };
    }

    private Map<Long, UserProfile> loadProfiles(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userProfileRepository.findAllByUserIdInAndDeletedAtIsNull(userIds).stream()
            .filter(profile -> profile.getUser() != null)
            .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity(), (oldValue, newValue) -> oldValue));
    }

    private UserAccount getAccountById(Long id) {
        return userAccountRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean isSelf(UserAccount account, String actorPhone) {
        return actorPhone != null && actorPhone.equals(account.getPhone());
    }

    private void applyProfile(UserProfile profile, UserProfilePayload payload) {
        profile.setFullName(payload.getFullName());
        profile.setPosition(payload.getPosition());
        profile.setUnitName(payload.getUnitName());
        profile.setRankName(payload.getRankName());
        profile.setEmail(payload.getEmail());
        profile.setAddress(payload.getAddress());
        profile.setBirthDate(payload.getBirthDate());
    }

    private void applyProfilePatch(UserProfile profile, UpdateUserProfilePayload payload) {
        if (payload.getFullName() != null) {
            profile.setFullName(payload.getFullName());
        }
        if (payload.getPosition() != null) {
            profile.setPosition(payload.getPosition());
        }
        if (payload.getUnitName() != null) {
            profile.setUnitName(payload.getUnitName());
        }
        if (payload.getRankName() != null) {
            profile.setRankName(payload.getRankName());
        }
        if (payload.getEmail() != null) {
            profile.setEmail(payload.getEmail());
        }
        if (payload.getAddress() != null) {
            profile.setAddress(payload.getAddress());
        }
        if (payload.getBirthDate() != null) {
            profile.setBirthDate(payload.getBirthDate());
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
        if (!PASSWORD_UPPER.matcher(password).matches()
            || !PASSWORD_LOWER.matcher(password).matches()
            || !PASSWORD_DIGIT.matcher(password).matches()) {
            throw new ApiException(
                ErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST,
                "Password must include uppercase, lowercase and digit"
            );
        }
    }

    private <T> T valueOrDefault(T requestedValue, T currentValue) {
        return requestedValue != null ? requestedValue : currentValue;
    }
}
