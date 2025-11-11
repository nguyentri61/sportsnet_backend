package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tournament_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private TournamentCategory category;

    @Enumerated(EnumType.STRING)
    TournamentParticipantEnum status;


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
