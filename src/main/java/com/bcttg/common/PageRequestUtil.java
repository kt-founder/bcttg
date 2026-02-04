package com.bcttg.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequestUtil {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private PageRequestUtil() {
    }

    public static Pageable build(Integer page, Integer pageSize, String sort, String order, Sort defaultSort) {
        int safePage = page != null && page > 0 ? page - 1 : 0;
        int safeSize = pageSize != null && pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        Sort sortObj = defaultSort;
        if (sort != null && !sort.isBlank()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sortObj = Sort.by(direction, sort);
        }
        return PageRequest.of(safePage, safeSize, sortObj);
    }
}
