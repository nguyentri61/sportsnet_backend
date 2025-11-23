package com.tlcn.sportsnet_backend.enums;

public enum TournamentParticipantEnum {
    PENDING,           // Chờ duyệt / đăng ký tạm thời
    PAYMENT_REQUIRED, // Đã đăng ký, cần thanh toán để xác nhận
    PAID,
    NOT_PAID,
    APPROVED,          // Đã thanh toán, được duyệt thi đấu
    REJECTED,          // Bị từ chối
    CANCELLED,         // Người chơi tự hủy
    ELIMINATED         // Đã bị loại trong giải
}
