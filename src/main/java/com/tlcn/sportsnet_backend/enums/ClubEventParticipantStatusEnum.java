package com.tlcn.sportsnet_backend.enums;

public enum ClubEventParticipantStatusEnum {
    PENDING, APPROVED, ATTENDED, ABSENT;
    public StatusScheduleEnum toStatusEnum() {
        return switch (this) {
            case PENDING -> StatusScheduleEnum.PENDING;
            case APPROVED -> StatusScheduleEnum.CONFIRMED;
            case ATTENDED -> StatusScheduleEnum.COMPLETED;
            case ABSENT -> StatusScheduleEnum.ABSENT;
            default -> null;
        };
    }
}
