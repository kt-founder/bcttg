package com.bcttg.module.dashboard.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bcttg.module.content.entity.ContentItem;
import com.bcttg.module.content.repository.ContentItemRepository;
import com.bcttg.module.dashboard.dto.AdminDashboardResponse;
import com.bcttg.module.dashboard.entity.SystemAuditLog;
import com.bcttg.module.dashboard.repository.SystemAuditLogRepository;
import com.bcttg.module.profile.entity.DataProfile;
import com.bcttg.module.profile.entity.ProfileType;
import com.bcttg.module.profile.repository.DataProfileRepository;
import com.bcttg.module.song.entity.Song;
import com.bcttg.module.song.repository.SongRepository;
import com.bcttg.module.user.repository.UserAccountRepository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {
    private final ContentItemRepository contentItemRepository;
    private final DataProfileRepository dataProfileRepository;
    private final SongRepository songRepository;
    private final UserAccountRepository userAccountRepository;
    private final SystemAuditLogRepository systemAuditLogRepository;

    public AdminDashboardService(
        ContentItemRepository contentItemRepository,
        DataProfileRepository dataProfileRepository,
        SongRepository songRepository,
        UserAccountRepository userAccountRepository,
        SystemAuditLogRepository systemAuditLogRepository
    ) {
        this.contentItemRepository = contentItemRepository;
        this.dataProfileRepository = dataProfileRepository;
        this.songRepository = songRepository;
        this.userAccountRepository = userAccountRepository;
        this.systemAuditLogRepository = systemAuditLogRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getOverview() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant now = Instant.now();
        Instant startOfToday = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant();
        Instant startOfTomorrow = LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId).toInstant();

        long totalPosts = countContentItems();
        long totalProfiles = dataProfileRepository.countByDeletedAtIsNull();
        long totalSongs = countSongs();
        long totalAccounts = userAccountRepository.countByDeletedAtIsNull();
        long viewsToday = systemAuditLogRepository.countByDeletedAtIsNullAndActionTypeAndCreatedAtBetween("VIEW", startOfToday, startOfTomorrow);
        long editsToday = systemAuditLogRepository.countByDeletedAtIsNullAndActionTypeInAndCreatedAtBetween(
            List.of("CREATE", "UPDATE", "DELETE"), startOfToday, startOfTomorrow);

        AdminDashboardResponse.Summary summary = new AdminDashboardResponse.Summary(
            totalPosts, totalProfiles, totalSongs, totalAccounts, viewsToday, editsToday);

        List<AdminDashboardResponse.LabelValueItem> monthlyContent = buildMonthlyContent(now, zoneId);
        List<AdminDashboardResponse.LabelValueItem> contentDistribution = buildDistribution();
        List<AdminDashboardResponse.LabelValueItem> weeklyVisits = buildWeeklyVisits(now, zoneId);
        List<AdminDashboardResponse.ActivityItem> recentActivities = buildRecentActivities();
        List<AdminDashboardResponse.StatusItem> systemStatuses = buildSystemStatuses(totalAccounts, totalPosts, totalSongs, totalProfiles);
        List<AdminDashboardResponse.PendingItem> pendingItems = buildPendingItems();

        return new AdminDashboardResponse(
            summary,
            monthlyContent,
            contentDistribution,
            weeklyVisits,
            recentActivities,
            systemStatuses,
            pendingItems
        );
    }

    private long countContentItems() {
        Specification<ContentItem> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        return contentItemRepository.count(spec);
    }

    private long countSongs() {
        Specification<Song> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        return songRepository.count(spec);
    }

    private List<AdminDashboardResponse.LabelValueItem> buildMonthlyContent(Instant now, ZoneId zoneId) {
        int year = LocalDate.ofInstant(now, zoneId).getYear();
        Map<Integer, Long> byMonth = new HashMap<>();
        for (Object[] row : contentItemRepository.countCreatedByMonth(year)) {
            int month = ((Number) row[0]).intValue();
            long total = ((Number) row[1]).longValue();
            byMonth.put(month, total);
        }

        List<AdminDashboardResponse.LabelValueItem> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            result.add(new AdminDashboardResponse.LabelValueItem("T" + month, byMonth.getOrDefault(month, 0L)));
        }
        return result;
    }

    private List<AdminDashboardResponse.LabelValueItem> buildDistribution() {
        long truyenThong = 0L;
        long netTieuBieu = 0L;
        long soDoLichSu = 0L;
        for (Object[] row : contentItemRepository.countByCategoryType()) {
            String type = String.valueOf(row[0]);
            long total = ((Number) row[1]).longValue();
            if ("TRUYEN_THONG".equals(type)) {
                truyenThong = total;
            } else if ("NET_TIEU_BIEU".equals(type)) {
                netTieuBieu = total;
            } else if ("SO_DO_LICH_SU".equals(type)) {
                soDoLichSu = total;
            }
        }

        long hoSoThuTruong = dataProfileRepository.countByDeletedAtIsNullAndProfileType(ProfileType.THU_TRUONG);
        long hoSoChienSi = dataProfileRepository.countByDeletedAtIsNullAndProfileType(ProfileType.CHIEN_SI);
        long hoSoAnhHung = dataProfileRepository.countByDeletedAtIsNullAndProfileType(ProfileType.ANH_HUNG);
        long caKhuc = countSongs();

        return List.of(
            new AdminDashboardResponse.LabelValueItem("Truyen thong", truyenThong),
            new AdminDashboardResponse.LabelValueItem("Net tieu bieu", netTieuBieu),
            new AdminDashboardResponse.LabelValueItem("So do lich su", soDoLichSu),
            new AdminDashboardResponse.LabelValueItem("Ho so thu truong", hoSoThuTruong),
            new AdminDashboardResponse.LabelValueItem("Ho so chien si", hoSoChienSi),
            new AdminDashboardResponse.LabelValueItem("Ho so anh hung", hoSoAnhHung),
            new AdminDashboardResponse.LabelValueItem("Ca khuc", caKhuc)
        );
    }

    private List<AdminDashboardResponse.LabelValueItem> buildWeeklyVisits(Instant now, ZoneId zoneId) {
        LocalDate today = LocalDate.ofInstant(now, zoneId);
        LocalDate fromDate = today.minusDays(6);
        Instant fromTime = fromDate.atStartOfDay(zoneId).toInstant();
        Instant toTime = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        Map<LocalDate, Long> totals = new HashMap<>();
        for (Object[] row : systemAuditLogRepository.countGroupedByDate("VIEW", fromTime, toTime)) {
            LocalDate day = toLocalDate(row[0], zoneId);
            long count = ((Number) row[1]).longValue();
            totals.put(day, count);
        }

        List<AdminDashboardResponse.LabelValueItem> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = fromDate.plusDays(i);
            result.add(new AdminDashboardResponse.LabelValueItem(dayLabel(d.getDayOfWeek()), totals.getOrDefault(d, 0L)));
        }
        return result;
    }

    private List<AdminDashboardResponse.ActivityItem> buildRecentActivities() {
        return systemAuditLogRepository.findTop10ByDeletedAtIsNullOrderByCreatedAtDesc().stream()
            .map(this::mapActivity)
            .toList();
    }

    private AdminDashboardResponse.ActivityItem mapActivity(SystemAuditLog log) {
        String title = log.getActionType() + " " + log.getModuleName();
        return new AdminDashboardResponse.ActivityItem(
            title,
            log.getActorName(),
            log.getDetail(),
            log.getCreatedAt(),
            log.getStatus()
        );
    }

    private List<AdminDashboardResponse.StatusItem> buildSystemStatuses(
        long totalAccounts,
        long totalPosts,
        long totalSongs,
        long totalProfiles
    ) {
        String dataLoad = (totalPosts + totalSongs + totalProfiles) > 0 ? "On dinh" : "Can khoi tao du lieu";
        String accountStatus = totalAccounts > 0 ? (totalAccounts + " tai khoan dang hoat dong") : "Chua co tai khoan";
        Instant lastActionAt = systemAuditLogRepository.findTop10ByDeletedAtIsNullOrderByCreatedAtDesc().stream()
            .map(SystemAuditLog::getCreatedAt)
            .findFirst()
            .orElse(Instant.now());
        String lastAction = "Lan cuoi " + ChronoUnit.MINUTES.between(lastActionAt, Instant.now()) + " phut truoc";

        return List.of(
            new AdminDashboardResponse.StatusItem("Co so du lieu", dataLoad, "GOOD"),
            new AdminDashboardResponse.StatusItem("Tai khoan", accountStatus, "GOOD"),
            new AdminDashboardResponse.StatusItem("Nhat ky he thong", lastAction, "GOOD"),
            new AdminDashboardResponse.StatusItem("Bao mat", "Da bat JWT bearer token", "GOOD"),
            new AdminDashboardResponse.StatusItem("SSL/TLS", "Quan ly tai lop reverse proxy", "INFO")
        );
    }

    private List<AdminDashboardResponse.PendingItem> buildPendingItems() {
        List<PendingWrapper> wrappers = new ArrayList<>();

        for (ContentItem item : contentItemRepository.findTop5ByDeletedAtIsNullAndIsVisibleFalseOrderByUpdatedAtDesc()) {
            String type = item.getCategory() != null && item.getCategory().getType() != null
                ? item.getCategory().getType().name()
                : "CONTENT";
            wrappers.add(new PendingWrapper(
                new AdminDashboardResponse.PendingItem(item.getTitle(), type, "He thong", item.getCreatedAt(), "CHO_DUYET"),
                item.getCreatedAt()
            ));
        }
        for (DataProfile profile : dataProfileRepository.findTop5ByDeletedAtIsNullAndIsVisibleFalseOrderByUpdatedAtDesc()) {
            wrappers.add(new PendingWrapper(
                new AdminDashboardResponse.PendingItem(profile.getFullName(), "HO_SO_" + profile.getProfileType().name(), profile.getCreatedByPhone(), profile.getCreatedAt(), "CHO_DUYET"),
                profile.getCreatedAt()
            ));
        }
        for (Song song : songRepository.findTop5ByDeletedAtIsNullAndIsVisibleFalseOrderByUpdatedAtDesc()) {
            wrappers.add(new PendingWrapper(
                new AdminDashboardResponse.PendingItem(song.getTitle(), "CA_KHUC", "He thong", song.getCreatedAt(), "CHO_DUYET"),
                song.getCreatedAt()
            ));
        }

        return wrappers.stream()
            .sorted(Comparator.comparing(PendingWrapper::createdAt).reversed())
            .limit(10)
            .map(PendingWrapper::item)
            .toList();
    }

    private String dayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "T2";
            case TUESDAY -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY -> "T5";
            case FRIDAY -> "T6";
            case SATURDAY -> "T7";
            case SUNDAY -> "CN";
        };
    }

    private LocalDate toLocalDate(Object value, ZoneId zoneId) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant().atZone(zoneId).toLocalDate();
        }
        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant().atZone(zoneId).toLocalDate();
        }
        if (value instanceof CharSequence text) {
            return LocalDate.parse(text);
        }
        throw new IllegalStateException("Unsupported date value type: " + (value == null ? "null" : value.getClass().getName()));
    }

    private record PendingWrapper(AdminDashboardResponse.PendingItem item, Instant createdAt) {
    }
}
