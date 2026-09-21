package com.bcttg.common;

import java.util.Map;

public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final Map<String, Object> meta;
    private final ApiError error;

    private ApiResponse(boolean success, T data, Map<String, Object> meta, ApiError error) {
        this.success = success;
        this.data = data;
        this.meta = meta;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return new ApiResponse<>(true, data, meta, null);
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return new ApiResponse<>(false, null, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public ApiError getError() {
        return error;
    }
}
