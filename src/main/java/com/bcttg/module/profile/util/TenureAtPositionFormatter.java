package com.bcttg.module.profile.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.profile.entity.ProfileType;

import org.springframework.http.HttpStatus;

public final class TenureAtPositionFormatter {
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TenureAtPositionFormatter() {
    }

    public static void validate(ProfileType profileType, LocalDate tenureFromDate, LocalDate tenureToDate, String tenureAtPositionFormat) {
        if (profileType != ProfileType.THU_TRUONG) {
            return;
        }
        if (tenureFromDate == null || tenureToDate == null || tenureAtPositionFormat == null || tenureAtPositionFormat.isBlank()) {
            throw new ApiException(
                ErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST,
                "Thủ trưởng bắt buộc nhập tenureFromDate, tenureToDate và tenureAtPositionFormat"
            );
        }
        LocalDate today = LocalDate.now();
        if (tenureFromDate.isAfter(today)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "tenureFromDate không được lớn hơn ngày hiện tại");
        }
        if (tenureToDate.isAfter(today)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "tenureToDate không được lớn hơn ngày hiện tại");
        }
        if (tenureToDate.isBefore(tenureFromDate)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "tenureToDate phải lớn hơn hoặc bằng tenureFromDate");
        }
        if (!tenureAtPositionFormat.contains("{from}") || !tenureAtPositionFormat.contains("{to}")) {
            throw new ApiException(
                ErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST,
                "tenureAtPositionFormat phải chứa đủ biến {from} và {to}"
            );
        }
    }

    public static String format(LocalDate tenureFromDate, LocalDate tenureToDate, String tenureAtPositionFormat) {
        if (tenureFromDate == null || tenureToDate == null || tenureAtPositionFormat == null || tenureAtPositionFormat.isBlank()) {
            return null;
        }
        String toValue = tenureToDate.equals(LocalDate.now())
            ? "hiện tại"
            : tenureToDate.format(DISPLAY_DATE_FORMAT);
        return tenureAtPositionFormat
            .replace("{from}", tenureFromDate.format(DISPLAY_DATE_FORMAT))
            .replace("{to}", toValue);
    }
}
