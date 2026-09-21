package com.bcttg.module.home.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.content.entity.ContentType;
import com.bcttg.module.content.repository.ContentItemRepository;
import com.bcttg.module.dashboard.service.SystemAuditTrailService;
import com.bcttg.module.home.dto.HomeModuleResponse;
import com.bcttg.module.home.dto.UpdateHomeModuleItemRequest;
import com.bcttg.module.home.dto.UpdateHomeModulesRequest;
import com.bcttg.module.home.entity.HomeModuleConfig;
import com.bcttg.module.home.entity.HomeModuleId;
import com.bcttg.module.home.repository.HomeModuleRepository;
import com.bcttg.module.profile.entity.ProfileType;
import com.bcttg.module.profile.repository.DataProfileRepository;
import com.bcttg.module.song.repository.SongRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeModuleService {
    private final HomeModuleRepository repository;
    private final ContentItemRepository contentItemRepository;
    private final DataProfileRepository dataProfileRepository;
    private final SongRepository songRepository;
    private final SystemAuditTrailService auditTrailService;
    private final PublicModuleAccessService publicModuleAccessService;

    public HomeModuleService(
        HomeModuleRepository repository,
        ContentItemRepository contentItemRepository,
        DataProfileRepository dataProfileRepository,
        SongRepository songRepository,
        SystemAuditTrailService auditTrailService,
        PublicModuleAccessService publicModuleAccessService
    ) {
        this.repository = repository;
        this.contentItemRepository = contentItemRepository;
        this.dataProfileRepository = dataProfileRepository;
        this.songRepository = songRepository;
        this.auditTrailService = auditTrailService;
        this.publicModuleAccessService = publicModuleAccessService;
    }

    @Transactional(readOnly = true)
    public List<HomeModuleResponse> getAdminModules() {
        return repository.findAllByOrderBySortOrderAscIdAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<HomeModuleResponse> getPublicModules(Authentication authentication) {
        List<HomeModuleConfig> modules = publicModuleAccessService.isAuthenticated(authentication)
            ? repository.findAllByEnabledTrueOrderBySortOrderAscIdAsc()
            : repository.findAllByEnabledTrueAndIsGuestTrueOrderBySortOrderAscIdAsc();
        return modules.stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public List<HomeModuleResponse> saveAll(UpdateHomeModulesRequest request, String actorPhone) {
        List<HomeModuleConfig> existing = repository.findAllByOrderBySortOrderAscIdAsc();
        validateModules(existing, request.getModules());

        Map<String, HomeModuleConfig> byId = existing.stream()
            .collect(Collectors.toMap(HomeModuleConfig::getId, module -> module));

        for (UpdateHomeModuleItemRequest item : request.getModules()) {
            HomeModuleConfig module = byId.get(normalize(item.getId()));
            module.setName(item.getName().trim());
            module.setDescription(item.getDescription());
            module.setEnabled(item.getEnabled());
            module.setIsGuest(item.getIsGuest());
            module.setSortOrder(item.getSortOrder());
            module.setUpdatedBy(actorPhone);
        }

        List<HomeModuleConfig> saved = repository.saveAll(existing);
        auditTrailService.record(actorPhone, "UPDATE", "HOME_MODULE", "home_modules", "cập nhật cấu hình module trang chủ");
        return saved.stream()
            .sorted((left, right) -> {
                int compareSort = Integer.compare(left.getSortOrder(), right.getSortOrder());
                if (compareSort != 0) {
                    return compareSort;
                }
                return left.getId().compareTo(right.getId());
            })
            .map(this::toResponse)
            .toList();
    }

    private void validateModules(List<HomeModuleConfig> existing, List<UpdateHomeModuleItemRequest> requested) {
        if (existing.isEmpty()) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Home modules not initialized");
        }
        Set<String> requestedIds = requested.stream()
            .map(item -> normalize(item.getId()))
            .collect(Collectors.toSet());
        Set<String> existingIds = existing.stream().map(HomeModuleConfig::getId).collect(Collectors.toSet());
         if (!requestedIds.equals(existingIds)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Home module payload must include all configured modules");
        }
        Set<Integer> sortOrders = new HashSet<>();
        for (UpdateHomeModuleItemRequest item : requested) {
            if (!sortOrders.add(item.getSortOrder())) {
                throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Sort order must be unique");
            }
        }
    }

    private HomeModuleResponse toResponse(HomeModuleConfig module) {
        return new HomeModuleResponse(
            module.getId(),
            module.getName(),
            module.getDescription(),
            module.getEnabled(),
            module.getIsGuest(),
            module.getSortOrder(),
            resolveItemCount(module.getId()),
            module.getUpdatedAt()
        );
    }

    private Long resolveItemCount(String moduleId) {
        Map<String, LongSupplier> suppliers = Map.of(
            HomeModuleId.BANNER.getValue(), () -> 0L,
            HomeModuleId.TRUYEN_THONG.getValue(), () -> contentItemRepository.countVisibleByCategoryType(ContentType.TRUYEN_THONG),
            HomeModuleId.NET_TIEU_BIEU.getValue(), () -> contentItemRepository.countVisibleByCategoryType(ContentType.NET_TIEU_BIEU),
            HomeModuleId.THU_TRUONG.getValue(), () -> dataProfileRepository.countByDeletedAtIsNullAndIsVisibleTrueAndProfileType(ProfileType.THU_TRUONG),
            HomeModuleId.ANH_HUNG.getValue(), () -> dataProfileRepository.countByDeletedAtIsNullAndIsVisibleTrueAndProfileType(ProfileType.ANH_HUNG),
            HomeModuleId.CA_KHUC.getValue(), () -> songRepository.countByDeletedAtIsNullAndIsVisibleTrue(),
            HomeModuleId.TIN_TUC.getValue(), () -> contentItemRepository.countVisibleByCategoryType(ContentType.TIN_TUC)
        );
        LongSupplier supplier = suppliers.get(moduleId);
        return supplier != null ? supplier.getAsLong() : 0L;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
