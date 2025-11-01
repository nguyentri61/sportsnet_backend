package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import com.tlcn.sportsnet_backend.enums.WarningStatus;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "club_warnings")
public class ClubWarning {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "club_member_id")
    ClubMember clubMember;
    private String reason;

    @Enumerated(EnumType.STRING)
    private WarningStatus status; // ACTIVE, REVOKED
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant createdAt;

    Instant updatedAt;

    String createdBy;

    String updatedBy;
    @PrePersist
    public void handleBeforeCreate() {
        createdAt = Instant.now();
        status = WarningStatus.ACTIVE;
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

}
