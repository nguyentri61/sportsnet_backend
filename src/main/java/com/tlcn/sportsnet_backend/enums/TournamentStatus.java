package com.tlcn.sportsnet_backend.enums;

public enum TournamentStatus {
    UPCOMING, // Sắp diễn ra (chưa tới ngày mở đăng ký)
    REGISTRATION_OPEN, // Đang mở đăng ký
    REGISTRATION_CLOSED, // Đã đóng đăng ký (chờ khai mạc)
    IN_PROGRESS, // Đang diễn ra
    COMPLETED, // Đã kết thúc
    CANCELLED // Bị hủy
}
