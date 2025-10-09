package com.tlcn.sportsnet_backend.enums;

public enum StatusScheduleEnum {
    PENDING,
    CONFIRMED,
    ONGOING,
    COMPLETED,
    CANCELLED,
        ABSENT,
    REJECTED;

    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case ONGOING -> "Đang diễn ra";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            case REJECTED -> "Bị từ chối";
            default -> this.name();
        };
    }
}
