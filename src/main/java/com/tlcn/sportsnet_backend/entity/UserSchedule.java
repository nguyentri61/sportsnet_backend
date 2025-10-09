package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlcn.sportsnet_backend.enums.StatusScheduleEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user_schedules")
public class UserSchedule {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    String id;

    String name;
    // Liên kết đến tài khoản
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    Account account;

    // Liên kết đến sự kiện (có thể null nếu user chưa có sự kiện)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_event_id", nullable = true)
    ClubEvent clubEvent;

    // Thời gian bắt đầu của lịch
    LocalDateTime startTime;

    // Thời gian kết thúc của lịch
    LocalDateTime endTime;

    // Trạng thái (ví dụ: Đã xác nhận, Đang diễn ra, Đã hủy)
    @Enumerated(EnumType.STRING)
    StatusScheduleEnum status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant createdAt;

    Instant updatedAt;

    String createdBy;

    String updatedBy;

    @PreUpdate
    public void handleBeforeUpdate(){
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

    }


}
