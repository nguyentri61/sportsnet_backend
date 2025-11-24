package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tournament_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    // Tên đội (nếu có)
    private String teamName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private TournamentCategory category;

    // 2 thành viên trong đội
    @ManyToOne
    @JoinColumn(name = "player1_id")
    private Account player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    private Account player2;

    private TournamentParticipantEnum status; // PENDING, APPROVED, PLAYING, ELIMINATED
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
        createTeamName();
    }

    @PreUpdate
    public void handleBeforeUpdate(){
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

    }

    public void createTeamName(){
        String[] parts1 = player1.getUserInfo().getFullName().trim().split("\\s+");
        String lastName1 = parts1[parts1.length - 1];

        String[] parts2 = player2.getUserInfo().getFullName().trim().split("\\s+");
        String lastName2 = parts2[parts2.length - 1];
        teamName = lastName1 + " " + lastName2;
    }
}
