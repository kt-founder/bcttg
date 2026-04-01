package com.bcttg.module.report.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.bcttg.common.ApiException;
import com.bcttg.common.AuditDetailUtil;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.content.entity.ContentItem;
import com.bcttg.module.content.repository.ContentItemRepository;
import com.bcttg.module.dashboard.entity.SystemAuditLog;
import com.bcttg.module.dashboard.repository.SystemAuditLogRepository;
import com.bcttg.module.report.dto.ReportOverviewResponse;
import com.bcttg.module.report.dto.ReportSummaryCardResponse;
import com.bcttg.module.report.dto.ReportTopContentItemResponse;
import com.bcttg.module.report.dto.ReportTrendSeriesItemResponse;
import com.bcttg.module.report.dto.ReportUserActivityItemResponse;
import com.bcttg.module.user.entity.UserRole;
import com.bcttg.module.user.repository.UserAccountRepository;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReportService {
    private final SystemAuditLogRepository auditLogRepository;
    private final ContentItemRepository contentItemRepository;
    private final UserAccountRepository userAccountRepository;

    public AdminReportService(
        SystemAuditLogRepository auditLogRepository,
        ContentItemRepository contentItemRepository,
        UserAccountRepository userAccountRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.contentItemRepository = contentItemRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public ReportOverviewResponse getOverview(String period) {
        ReportWindow reportWindow = resolveWindow(period);
        List<SystemAuditLog> currentLogs = auditLogRepository.findAll(buildTimeSpecification(reportWindow.currentFrom(), reportWindow.currentTo()), Sort.by("createdAt").ascending());
        List<SystemAuditLog> previousLogs = auditLogRepository.findAll(buildTimeSpecification(reportWindow.previousFrom(), reportWindow.previousTo()), Sort.by("createdAt").ascending());
        return new ReportOverviewResponse(
            buildSummaryCards(currentLogs, previousLogs),
            buildTrendSeries(reportWindow, currentLogs),
            buildUserActivity(reportWindow),
            buildTopContent(reportWindow, currentLogs, previousLogs)
        );
    }

    @Transactional(readOnly = true)
    public ByteArrayResource export(String type, String period, String format) {
        if (!"xlsx".equalsIgnoreCase(format)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Chi ho tro dinh dang xlsx");
        }
        ReportOverviewResponse overview = getOverview(period);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String normalizedType = type == null || type.isBlank() ? "overview" : type.toLowerCase(Locale.ROOT);
            switch (normalizedType) {
                case "overview" -> {
                    writeSummarySheet(workbook.createSheet("summary"), overview.getSummaryCards());
                    writeTrendSheet(workbook.createSheet("trend"), overview.getTrendSeries());
                    writeUserActivitySheet(workbook.createSheet("user_activity"), overview.getUserActivity());
                    writeTopContentSheet(workbook.createSheet("top_content"), overview.getTopContent());
                }
                case "summary" -> writeSummarySheet(workbook.createSheet("summary"), overview.getSummaryCards());
                case "trend" -> writeTrendSheet(workbook.createSheet("trend"), overview.getTrendSeries());
                case "user-activity" -> writeUserActivitySheet(workbook.createSheet("user_activity"), overview.getUserActivity());
                case "top-content" -> writeTopContentSheet(workbook.createSheet("top_content"), overview.getTopContent());
                default -> throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Loai export bao cao khong hop le");
            }
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Khong tao duoc file bao cao", List.of(ex.getMessage()));
        }
    }

    private List<ReportSummaryCardResponse> buildSummaryCards(List<SystemAuditLog> currentLogs, List<SystemAuditLog> previousLogs) {
        long currentViews = currentLogs.stream().filter(this::isViewLog).count();
        long previousViews = previousLogs.stream().filter(this::isViewLog).count();
        long totalViews = contentItemRepository.sumViewCount();

        long currentActions = currentLogs.stream().filter(this::isActionLog).count();
        long previousActions = previousLogs.stream().filter(this::isActionLog).count();

        long currentActiveUsers = currentLogs.stream().map(this::resolveActorKey).filter(Objects::nonNull).distinct().count();
        long previousActiveUsers = previousLogs.stream().map(this::resolveActorKey).filter(Objects::nonNull).distinct().count();

        double currentAverageMinutes = calculateAverageMinutes(currentLogs);
        double previousAverageMinutes = calculateAverageMinutes(previousLogs);

        return List.of(
            new ReportSummaryCardResponse("views", "Tong luot xem", String.valueOf(totalViews), calculateChange(currentViews, previousViews), "so voi ky truoc", "views"),
            new ReportSummaryCardResponse("actions", "Tong thao tac", String.valueOf(currentActions), calculateChange(currentActions, previousActions), "so voi ky truoc", "actions"),
            new ReportSummaryCardResponse("active_users", "Nguoi dung hoat dong", String.valueOf(currentActiveUsers), calculateChange(currentActiveUsers, previousActiveUsers), "so voi ky truoc", "users"),
            new ReportSummaryCardResponse("avg_time", "Thoi gian trung binh", formatDuration(currentAverageMinutes), calculateChange(currentAverageMinutes, previousAverageMinutes), "phut/phien", "time")
        );
    }

    private List<ReportTrendSeriesItemResponse> buildTrendSeries(ReportWindow reportWindow, List<SystemAuditLog> currentLogs) {
        List<ReportTrendSeriesItemResponse> items = new ArrayList<>();
        for (ReportBucket bucket : reportWindow.buckets()) {
            long views = currentLogs.stream()
                .filter(this::isViewLog)
                .filter(log -> isInBucket(log.getCreatedAt(), bucket))
                .count();
            long edits = currentLogs.stream()
                .filter(this::isActionLog)
                .filter(log -> isInBucket(log.getCreatedAt(), bucket))
                .count();
            long logins = currentLogs.stream()
                .filter(this::isLoginLog)
                .filter(log -> isInBucket(log.getCreatedAt(), bucket))
                .count();
            items.add(new ReportTrendSeriesItemResponse(bucket.label(), views, edits, logins));
        }
        return items;
    }

    private List<ReportUserActivityItemResponse> buildUserActivity(ReportWindow reportWindow) {
        Map<String, Long> currentByRole = new HashMap<>();
        for (Object[] row : auditLogRepository.countDistinctActorsByRole(reportWindow.currentFrom(), reportWindow.currentTo())) {
            currentByRole.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return List.of(
            buildUserActivityItem(UserRole.ADMIN, "Quan tri vien", currentByRole),
            buildUserActivityItem(UserRole.MANAGER, "Quan ly", currentByRole),
            buildUserActivityItem(UserRole.USER, "Nguoi dung", currentByRole)
        );
    }

    private ReportUserActivityItemResponse buildUserActivityItem(UserRole role, String label, Map<String, Long> currentByRole) {
        long activeAccounts = userAccountRepository.countByDeletedAtIsNullAndRoleAndIsActiveTrue(role);
        long currentActors = currentByRole.getOrDefault(role.name(), 0L);
        double activity = activeAccounts == 0L ? 0D : roundOneDecimal((currentActors * 100D) / activeAccounts);
        return new ReportUserActivityItemResponse(label, currentActors, activity);
    }

    private List<ReportTopContentItemResponse> buildTopContent(ReportWindow reportWindow, List<SystemAuditLog> currentLogs, List<SystemAuditLog> previousLogs) {
        Map<Long, Long> currentViewByContent = countViewsByContent(currentLogs);
        Map<Long, Long> previousViewByContent = countViewsByContent(previousLogs);
        return contentItemRepository.findTop10ByDeletedAtIsNullOrderByViewCountDescUpdatedAtDesc().stream()
            .map(item -> new ReportTopContentItemResponse(
                item.getTitle(),
                item.getViewCount() == null ? 0L : item.getViewCount().longValue(),
                calculateChange(currentViewByContent.getOrDefault(item.getId(), 0L), previousViewByContent.getOrDefault(item.getId(), 0L))
            ))
            .toList();
    }

    private Map<Long, Long> countViewsByContent(List<SystemAuditLog> logs) {
        Map<Long, Long> totals = new LinkedHashMap<>();
        for (SystemAuditLog log : logs) {
            if (!isViewLog(log)) {
                continue;
            }
            String contentId = AuditDetailUtil.decode(log.getDetail()).get("content_id");
            if (contentId == null || contentId.isBlank()) {
                continue;
            }
            Long id = Long.parseLong(contentId);
            totals.put(id, totals.getOrDefault(id, 0L) + 1L);
        }
        return totals;
    }

    private double calculateAverageMinutes(List<SystemAuditLog> logs) {
        Map<String, List<Instant>> byActor = new HashMap<>();
        for (SystemAuditLog log : logs) {
            String actorKey = resolveActorKey(log);
            if (actorKey == null) {
                continue;
            }
            byActor.computeIfAbsent(actorKey, key -> new ArrayList<>()).add(log.getCreatedAt());
        }
        List<Double> averages = new ArrayList<>();
        for (List<Instant> instants : byActor.values()) {
            if (instants.size() < 2) {
                continue;
            }
            instants.sort(Comparator.naturalOrder());
            long totalSeconds = 0L;
            for (int index = 1; index < instants.size(); index++) {
                totalSeconds += java.time.Duration.between(instants.get(index - 1), instants.get(index)).getSeconds();
            }
            averages.add(totalSeconds / 60D / (instants.size() - 1));
        }
        if (averages.isEmpty()) {
            return 0D;
        }
        return roundOneDecimal(averages.stream().mapToDouble(Double::doubleValue).average().orElse(0D));
    }

    private ReportWindow resolveWindow(String period) {
        String effectivePeriod = period == null || period.isBlank() ? "month" : period.toLowerCase(Locale.ROOT);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zoneId);
        return switch (effectivePeriod) {
            case "week" -> buildDailyWindow(today.minusDays(6), 7, zoneId, true);
            case "month" -> buildDailyWindow(today.minusDays(29), 30, zoneId, false);
            case "quarter" -> buildWeeklyWindow(today.minusWeeks(12).with(java.time.DayOfWeek.MONDAY), 13, zoneId);
            case "year" -> buildMonthlyWindow(today.withDayOfYear(1), zoneId);
            default -> throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Gia tri period khong hop le");
        };
    }

    private ReportWindow buildDailyWindow(LocalDate startDate, int days, ZoneId zoneId, boolean dayOfWeekLabel) {
        Instant currentFrom = startDate.atStartOfDay(zoneId).toInstant();
        Instant currentTo = startDate.plusDays(days).atStartOfDay(zoneId).toInstant();
        Instant previousFrom = startDate.minusDays(days).atStartOfDay(zoneId).toInstant();
        Instant previousTo = startDate.atStartOfDay(zoneId).toInstant();
        List<ReportBucket> buckets = new ArrayList<>();
        for (int index = 0; index < days; index++) {
            LocalDate bucketDate = startDate.plusDays(index);
            String label = dayOfWeekLabel ? mapDayLabel(bucketDate.getDayOfWeek().getValue()) : bucketDate.format(DateTimeFormatter.ofPattern("dd/MM"));
            buckets.add(new ReportBucket(label, bucketDate.atStartOfDay(zoneId).toInstant(), bucketDate.plusDays(1).atStartOfDay(zoneId).toInstant()));
        }
        return new ReportWindow(currentFrom, currentTo, previousFrom, previousTo, buckets);
    }

    private ReportWindow buildWeeklyWindow(LocalDate startMonday, int weeks, ZoneId zoneId) {
        Instant currentFrom = startMonday.atStartOfDay(zoneId).toInstant();
        Instant currentTo = startMonday.plusWeeks(weeks).atStartOfDay(zoneId).toInstant();
        Instant previousFrom = startMonday.minusWeeks(weeks).atStartOfDay(zoneId).toInstant();
        Instant previousTo = startMonday.atStartOfDay(zoneId).toInstant();
        List<ReportBucket> buckets = new ArrayList<>();
        for (int index = 0; index < weeks; index++) {
            LocalDate bucketStart = startMonday.plusWeeks(index);
            buckets.add(new ReportBucket("W" + (index + 1), bucketStart.atStartOfDay(zoneId).toInstant(), bucketStart.plusWeeks(1).atStartOfDay(zoneId).toInstant()));
        }
        return new ReportWindow(currentFrom, currentTo, previousFrom, previousTo, buckets);
    }

    private ReportWindow buildMonthlyWindow(LocalDate startYear, ZoneId zoneId) {
        LocalDate firstDay = startYear.withDayOfYear(1);
        Instant currentFrom = firstDay.atStartOfDay(zoneId).toInstant();
        Instant currentTo = firstDay.plusYears(1).atStartOfDay(zoneId).toInstant();
        Instant previousFrom = firstDay.minusYears(1).atStartOfDay(zoneId).toInstant();
        Instant previousTo = firstDay.atStartOfDay(zoneId).toInstant();
        List<ReportBucket> buckets = new ArrayList<>();
        LocalDate currentMonth = firstDay;
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = currentMonth.withMonth(month).with(TemporalAdjusters.firstDayOfMonth());
            buckets.add(new ReportBucket("T" + month, monthStart.atStartOfDay(zoneId).toInstant(), monthStart.plusMonths(1).atStartOfDay(zoneId).toInstant()));
        }
        return new ReportWindow(currentFrom, currentTo, previousFrom, previousTo, buckets);
    }

    private Specification<SystemAuditLog> buildTimeSpecification(Instant from, Instant to) {
        return Specification.<SystemAuditLog>where((root, query, builder) -> builder.isNull(root.get("deletedAt")))
            .and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("createdAt"), from))
            .and((root, query, builder) -> builder.lessThan(root.get("createdAt"), to));
    }

    private boolean isInBucket(Instant createdAt, ReportBucket bucket) {
        return !createdAt.isBefore(bucket.from()) && createdAt.isBefore(bucket.to());
    }

    private boolean isViewLog(SystemAuditLog log) {
        return "VIEW".equalsIgnoreCase(log.getActionType());
    }

    private boolean isActionLog(SystemAuditLog log) {
        return List.of("CREATE", "UPDATE", "DELETE", "RESTORE", "CLEAR_CACHE", "TEST_EMAIL").contains(log.getActionType().toUpperCase(Locale.ROOT));
    }

    private boolean isLoginLog(SystemAuditLog log) {
        return "LOGIN".equalsIgnoreCase(log.getActionType()) && !"FAILED".equalsIgnoreCase(log.getStatus());
    }

    private String resolveActorKey(SystemAuditLog log) {
        if (log.getActorUser() != null && log.getActorUser().getId() != null) {
            return "user-" + log.getActorUser().getId();
        }
        if (log.getEntityName() != null && !log.getEntityName().isBlank()) {
            return "entity-" + log.getEntityName();
        }
        if (log.getActorName() != null && !log.getActorName().isBlank()) {
            return "actor-" + log.getActorName();
        }
        return null;
    }

    private double calculateChange(double currentValue, double previousValue) {
        if (previousValue == 0D) {
            return currentValue == 0D ? 0D : 100D;
        }
        return roundOneDecimal(((currentValue - previousValue) / previousValue) * 100D);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10D) / 10D;
    }

    private String formatDuration(double minutes) {
        long totalSeconds = Math.round(minutes * 60D);
        long displayMinutes = totalSeconds / 60L;
        long displaySeconds = totalSeconds % 60L;
        return displayMinutes + ":" + String.format("%02d", displaySeconds);
    }

    private String mapDayLabel(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "T2";
            case 2 -> "T3";
            case 3 -> "T4";
            case 4 -> "T5";
            case 5 -> "T6";
            case 6 -> "T7";
            default -> "CN";
        };
    }

    private void writeSummarySheet(XSSFSheet sheet, List<ReportSummaryCardResponse> rows) {
        writeRow(sheet, 0, List.of("ID", "Tieu de", "Gia tri", "Thay doi", "Ky so sanh", "Icon"));
        for (int index = 0; index < rows.size(); index++) {
            ReportSummaryCardResponse row = rows.get(index);
            writeRow(sheet, index + 1, List.of(row.getId(), row.getTitle(), row.getValue(), String.valueOf(row.getChange()), row.getPeriod(), row.getIconKey()));
        }
        autosize(sheet, 6);
    }

    private void writeTrendSheet(XSSFSheet sheet, List<ReportTrendSeriesItemResponse> rows) {
        writeRow(sheet, 0, List.of("Nhan", "Luot xem", "Thao tac", "Dang nhap"));
        for (int index = 0; index < rows.size(); index++) {
            ReportTrendSeriesItemResponse row = rows.get(index);
            writeRow(sheet, index + 1, List.of(row.getLabel(), String.valueOf(row.getViews()), String.valueOf(row.getEdits()), String.valueOf(row.getLogins())));
        }
        autosize(sheet, 4);
    }

    private void writeUserActivitySheet(XSSFSheet sheet, List<ReportUserActivityItemResponse> rows) {
        writeRow(sheet, 0, List.of("Vai tro", "Nguoi dung hoat dong", "Ty le"));
        for (int index = 0; index < rows.size(); index++) {
            ReportUserActivityItemResponse row = rows.get(index);
            writeRow(sheet, index + 1, List.of(row.getName(), String.valueOf(row.getValue()), String.valueOf(row.getActivity())));
        }
        autosize(sheet, 3);
    }

    private void writeTopContentSheet(XSSFSheet sheet, List<ReportTopContentItemResponse> rows) {
        writeRow(sheet, 0, List.of("Tieu de", "Luot xem", "Xu huong"));
        for (int index = 0; index < rows.size(); index++) {
            ReportTopContentItemResponse row = rows.get(index);
            writeRow(sheet, index + 1, List.of(row.getTitle(), String.valueOf(row.getViews()), String.valueOf(row.getTrend())));
        }
        autosize(sheet, 3);
    }

    private void writeRow(XSSFSheet sheet, int rowIndex, List<String> values) {
        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex);
        for (int index = 0; index < values.size(); index++) {
            row.createCell(index).setCellValue(values.get(index));
        }
    }

    private void autosize(XSSFSheet sheet, int columnCount) {
        for (int index = 0; index < columnCount; index++) {
            sheet.autoSizeColumn(index);
        }
    }

    private record ReportBucket(String label, Instant from, Instant to) {
    }

    private record ReportWindow(Instant currentFrom, Instant currentTo, Instant previousFrom, Instant previousTo, List<ReportBucket> buckets) {
    }
}
