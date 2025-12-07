package com.tlcn.sportsnet_backend.enums;

import lombok.Getter;

@Getter
public enum BadmintonCategoryEnum {
    MEN_SINGLE("Đơn nam", "SINGLE"),
    WOMEN_SINGLE("Đơn nữ", "SINGLE"),
    MEN_DOUBLE("Đôi nam", "DOUBLE"),
    WOMEN_DOUBLE("Đôi nữ", "DOUBLE"),
    MIXED_DOUBLE("Đôi nam nữ", "DOUBLE");

    private final String label;
    private final String type;

    BadmintonCategoryEnum(String label, String type) {
        this.label = label;
        this.type = type;
    }
}
