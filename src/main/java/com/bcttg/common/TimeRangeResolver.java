package com.bcttg.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.http.HttpStatus;

public final class TimeRangeResolver {
    private TimeRangeResolver() {
    }

    public static TimeRange resolve(String period, Instant from, Instant to, ZoneId zoneId) {
        if (from != null && to != null) {
            return new TimeRange(from, to);
        }
        LocalDate today = LocalDate.now(zoneId);
        String effectivePeriod = period == null || period.isBlank() ? "month" : period.toLowerCase();
        return switch (effectivePeriod) {
            case "day" -> new TimeRange(today.atStartOfDay(zoneId).toInstant(), today.plusDays(1).atStartOfDay(zoneId).toInstant());
            case "week" -> new TimeRange(today.minusDays(6).atStartOfDay(zoneId).toInstant(), today.plusDays(1).atStartOfDay(zoneId).toInstant());
            case "month" -> new TimeRange(today.minusDays(29).atStartOfDay(zoneId).toInstant(), today.plusDays(1).atStartOfDay(zoneId).toInstant());
            case "quarter" -> new TimeRange(today.minusDays(89).atStartOfDay(zoneId).toInstant(), today.plusDays(1).atStartOfDay(zoneId).toInstant());
            case "year" -> new TimeRange(today.minusDays(364).atStartOfDay(zoneId).toInstant(), today.plusDays(1).atStartOfDay(zoneId).toInstant());
            default -> throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Gia tri period khong hop le");
        };
    }
}
