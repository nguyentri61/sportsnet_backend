package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;

@Entity
@Table(name = "tournament_partner_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentPartnerInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Người gửi lời mời
    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    private Account inviter;

    // Người được mời
    @ManyToOne
    @JoinColumn(name = "invitee_id", nullable = false)
    private Account invitee;

    // Trạng thái lời mời
    @Enumerated(EnumType.STRING)
    InvitationStatusEnum status;

    @Lob
    String message;

    // Thuộc hạng mục đôi nào
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private TournamentCategory category;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    @PrePersist
    public void handleBeforeCreate(){
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

    }

    @PreUpdate
    public void handleBeforeUpdate(){
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

    }
}
