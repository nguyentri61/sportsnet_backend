package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "club_invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    // Người nhận lời mời (người được mời tham gia)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    Account receiver;

    // CLB mà lời mời liên quan đến
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    Club club;

    // Nội dung lời mời (vd: "CLB A mời bạn tham gia đội cầu lông tại ABC")
    @Lob
    String message;

    // Trạng thái lời mời
    @Enumerated(EnumType.STRING)
    InvitationStatusEnum status;

    // Thời điểm gửi
    Instant sentAt;

    // Thời điểm người nhận phản hồi
    Instant respondedAt;

    // Người tạo & cập nhật
    String createdBy;
    String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        sentAt = Instant.now();
        status = InvitationStatusEnum.PENDING;
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
