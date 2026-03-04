package com.bcttg.module.profile.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.media.entity.MediaAsset;
import com.bcttg.module.media.repository.MediaAssetRepository;
import com.bcttg.module.profile.dto.CreateDataProfileRequest;
import com.bcttg.module.profile.dto.ReorderDataProfileRequest;
import com.bcttg.module.profile.dto.UpdateDataProfileRequest;
import com.bcttg.module.profile.entity.DataProfile;
import com.bcttg.module.profile.entity.ProfileType;
import com.bcttg.module.profile.repository.DataProfileRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataProfileService {
    private final DataProfileRepository repository;
    private final MediaAssetRepository mediaRepository;

    public DataProfileService(DataProfileRepository repository, MediaAssetRepository mediaRepository) {
        this.repository = repository;
        this.mediaRepository = mediaRepository;
    }

    public Page<DataProfile> findAll(ProfileType profileType, String q, Boolean isVisible, Pageable pageable) {
        Specification<DataProfile> spec = Specification.<DataProfile>where(
            (root, query, cb) -> cb.isNull(root.get("deletedAt"))
        );
        if (profileType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("profileType"), profileType));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("fullName")), like),
                cb.like(cb.lower(root.get("position")), like),
                cb.like(cb.lower(root.get("unitName")), like)
            ));
        }
        if (isVisible != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isVisible"), isVisible));
        }
        return repository.findAll(spec, pageable);
    }

    public Page<DataProfile> findAllPublic(ProfileType profileType, String q, Pageable pageable) {
        return findAll(profileType, q, true, pageable);
    }

    public DataProfile getById(Long id) {
        DataProfile profile = repository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Data profile not found"));
        if (profile.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Data profile not found");
        }
        return profile;
    }

    public DataProfile getVisibleById(Long id) {
        DataProfile profile = getById(id);
        if (!Boolean.TRUE.equals(profile.getIsVisible())) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Data profile not found");
        }
        return profile;
    }

    @Transactional
    public DataProfile create(CreateDataProfileRequest request, String createdByPhone) {
        MediaAsset avatarMedia = null;
        if (request.getAvatarMediaId() != null) {
            avatarMedia = mediaRepository.findById(request.getAvatarMediaId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Avatar media not found"));
        }

        DataProfile profile = new DataProfile();
        applyCreateOrUpdate(profile, request.getProfileType(), request.getFullName(), request.getPosition(),
            request.getUnitName(), request.getRankName(), request.getHeroTitle(), request.getContactPhone(),
            request.getBirthDate(), request.getHometown(), request.getSummary(), request.getBiography(),
            request.getAchievements(), avatarMedia);
        profile.setIsVisible(Optional.ofNullable(request.getIsVisible()).orElse(true));

        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            sortOrder = repository.findMaxSortOrder(request.getProfileType()) + 1;
        }
        profile.setSortOrder(sortOrder);
        profile.setCreatedByPhone(createdByPhone);
        return repository.save(profile);
    }

    @Transactional
    public DataProfile update(Long id, UpdateDataProfileRequest request) {
        DataProfile profile = getById(id);
        MediaAsset avatarMedia = null;
        if (request.getAvatarMediaId() != null) {
            avatarMedia = mediaRepository.findById(request.getAvatarMediaId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Avatar media not found"));
        }

        applyCreateOrUpdate(profile, request.getProfileType(), request.getFullName(), request.getPosition(),
            request.getUnitName(), request.getRankName(), request.getHeroTitle(), request.getContactPhone(),
            request.getBirthDate(), request.getHometown(), request.getSummary(), request.getBiography(),
            request.getAchievements(), avatarMedia);
        profile.setIsVisible(request.getIsVisible());
        profile.setSortOrder(request.getSortOrder());
        return repository.save(profile);
    }

    @Transactional
    public void delete(Long id) {
        DataProfile profile = getById(id);
        repository.delete(profile);
    }

    @Transactional
    public DataProfile updateVisibility(Long id, boolean isVisible) {
        DataProfile profile = getById(id);
        profile.setIsVisible(isVisible);
        return repository.save(profile);
    }

    @Transactional
    public void reorder(ReorderDataProfileRequest request) {
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Orders cannot be empty");
        }

        List<Long> ids = request.getOrders().stream().map(ReorderDataProfileRequest.OrderItem::getId).toList();
        List<DataProfile> profiles = repository.findAllById(ids);
        if (profiles.size() != ids.size()) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some data profiles not found");
        }

        for (DataProfile profile : profiles) {
            if (profile.getDeletedAt() != null) {
                throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Some data profiles not found");
            }
            if (profile.getProfileType() != request.getProfileType()) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Profile type mismatch in reorder scope");
            }
        }

        for (ReorderDataProfileRequest.OrderItem order : request.getOrders()) {
            profiles.stream()
                .filter(p -> p.getId().equals(order.getId()))
                .findFirst()
                .ifPresent(p -> p.setSortOrder(order.getSortOrder()));
        }
        repository.saveAll(profiles);
    }

    private void applyCreateOrUpdate(
        DataProfile profile,
        ProfileType profileType,
        String fullName,
        String position,
        String unitName,
        String rankName,
        String heroTitle,
        String contactPhone,
        java.time.LocalDate birthDate,
        String hometown,
        String summary,
        String biography,
        String achievements,
        MediaAsset avatarMedia
    ) {
        profile.setProfileType(profileType);
        profile.setFullName(fullName);
        profile.setPosition(position);
        profile.setUnitName(unitName);
        profile.setRankName(rankName);
        profile.setHeroTitle(heroTitle);
        profile.setContactPhone(contactPhone);
        profile.setBirthDate(birthDate);
        profile.setHometown(hometown);
        profile.setSummary(summary);
        profile.setBiography(biography);
        profile.setAchievements(achievements);
        profile.setAvatarMedia(avatarMedia);
    }
}
