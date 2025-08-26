package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.ClubMemberRoleEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "club_members")
public class ClubMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Enumerated(EnumType.STRING)
    ClubMemberRoleEnum role; // MEMBER, ADMIN

    @Enumerated(EnumType.STRING)
    ClubMemberStatusEnum status; // ACTIVE, BANNED, PENDING_APPROVAL

    String note;

    LocalDateTime joinedAt;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    Account account;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    Club club;
}
