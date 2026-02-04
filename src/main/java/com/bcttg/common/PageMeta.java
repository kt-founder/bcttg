package com.bcttg.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;

public final class PageMeta {
    private PageMeta() {
    }

    public static Map<String, Object> from(Page<?> page) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("page", page.getNumber() + 1);
        meta.put("page_size", page.getSize());
        meta.put("total_elements", page.getTotalElements());
        meta.put("total_pages", page.getTotalPages());
        return meta;
    }
}
