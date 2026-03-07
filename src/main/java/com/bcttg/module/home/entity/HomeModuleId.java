package com.bcttg.module.home.entity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum HomeModuleId {
    BANNER("banner"),
    TRUYEN_THONG("truyen-thong"),
    NET_TIEU_BIEU("net-tieu-bieu"),
    THU_TRUONG("thu-truong"),
    ANH_HUNG("anh-hung"),
    CA_KHUC("ca-khuc"),
    TIN_TUC("tin-tuc");

    private final String value;

    HomeModuleId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Set<String> valuesAsSet() {
        return Arrays.stream(values()).map(HomeModuleId::getValue).collect(Collectors.toSet());
    }
}
