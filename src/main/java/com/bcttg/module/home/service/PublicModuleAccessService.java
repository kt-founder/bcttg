package com.bcttg.module.home.service;

import java.util.EnumSet;
import java.util.Set;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.home.entity.HomeModuleId;
import com.bcttg.module.home.repository.HomeModuleRepository;
import com.bcttg.module.profile.entity.ProfileType;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class PublicModuleAccessService {
    private final HomeModuleRepository homeModuleRepository;

    public PublicModuleAccessService(HomeModuleRepository homeModuleRepository) {
        this.homeModuleRepository = homeModuleRepository;
    }

    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public void ensureSongAccess(Authentication authentication) {
        ensureModuleAccess(authentication, HomeModuleId.CA_KHUC);
    }

    public void ensureContentAccess(Authentication authentication, ContentType type) {
        HomeModuleId moduleId = toHomeModuleId(type);
        if (moduleId == null) {
            ensureAuthenticated(authentication);
            return;
        }
        ensureModuleAccess(authentication, moduleId);
    }

    public void ensureProfileAccess(Authentication authentication, ProfileType profileType) {
        HomeModuleId moduleId = toHomeModuleId(profileType);
        if (moduleId == null) {
            ensureAuthenticated(authentication);
            return;
        }
        ensureModuleAccess(authentication, moduleId);
    }

    public Set<ContentType> getGuestContentTypes() {
        Set<ContentType> types = EnumSet.noneOf(ContentType.class);
        if (isGuestModuleEnabled(HomeModuleId.TRUYEN_THONG)) {
            types.add(ContentType.TRUYEN_THONG);
        }
        if (isGuestModuleEnabled(HomeModuleId.NET_TIEU_BIEU)) {
            types.add(ContentType.NET_TIEU_BIEU);
        }
        return types;
    }

    public Set<ProfileType> getGuestProfileTypes() {
        Set<ProfileType> types = EnumSet.noneOf(ProfileType.class);
        if (isGuestModuleEnabled(HomeModuleId.THU_TRUONG)) {
            types.add(ProfileType.THU_TRUONG);
        }
        if (isGuestModuleEnabled(HomeModuleId.ANH_HUNG)) {
            types.add(ProfileType.ANH_HUNG);
        }
        return types;
    }

    public boolean isGuestSongAccessEnabled() {
        return isGuestModuleEnabled(HomeModuleId.CA_KHUC);
    }

    private void ensureModuleAccess(Authentication authentication, HomeModuleId moduleId) {
        if (isAuthenticated(authentication)) {
            return;
        }
        if (!isGuestModuleEnabled(moduleId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Module requires authentication");
        }
    }

    private void ensureAuthenticated(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Module requires authentication");
        }
    }

    private boolean isGuestModuleEnabled(HomeModuleId moduleId) {
        return homeModuleRepository.findById(moduleId.getValue())
            .map(module -> Boolean.TRUE.equals(module.getEnabled()) && Boolean.TRUE.equals(module.getIsGuest()))
            .orElse(false);
    }

    private HomeModuleId toHomeModuleId(ContentType type) {
        if (type == null) {
            return null;
        }
        if (type == ContentType.TRUYEN_THONG) {
            return HomeModuleId.TRUYEN_THONG;
        }
        if (type == ContentType.NET_TIEU_BIEU) {
            return HomeModuleId.NET_TIEU_BIEU;
        }
        return null;
    }

    private HomeModuleId toHomeModuleId(ProfileType profileType) {
        if (profileType == null) {
            return null;
        }
        if (profileType == ProfileType.THU_TRUONG) {
            return HomeModuleId.THU_TRUONG;
        }
        if (profileType == ProfileType.ANH_HUNG) {
            return HomeModuleId.ANH_HUNG;
        }
        return null;
    }
}
