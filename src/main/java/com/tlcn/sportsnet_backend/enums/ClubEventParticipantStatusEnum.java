package com.tlcn.sportsnet_backend.enums;

public enum ClubEventParticipantStatusEnum {
    PENDING, APPROVED, ATTENDED, ABSENT, CANCELLATION_PENDING, CANCELLED;
    public StatusScheduleEnum toStatusEnum() {
        return switch (this) {
            case PENDING -> StatusScheduleEnum.PENDING;
            case APPROVED -> StatusScheduleEnum.CONFIRMED;
            case ATTENDED -> StatusScheduleEnum.COMPLETED;
            case ABSENT -> StatusScheduleEnum.ABSENT;
            case CANCELLATION_PENDING -> StatusScheduleEnum.CANCELLATION_PENDING;
            case CANCELLED -> StatusScheduleEnum.CANCELLED;
            default -> null;
        };
    }
}
