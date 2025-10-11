package com.tlcn.sportsnet_backend.enums;

import lombok.Getter;

@Getter
public enum BadmintonCategoryEnum {
    MEN_SINGLE("Đơn nam"),
    WOMEN_SINGLE("Đơn nữ"),
    MEN_DOUBLE("Đôi nam"),
    WOMEN_DOUBLE("Đôi nữ"),
    MIXED_DOUBLE("Đôi nam nữ");

    private final String label;

    BadmintonCategoryEnum(String label) {
        this.label = label;
    }

}
