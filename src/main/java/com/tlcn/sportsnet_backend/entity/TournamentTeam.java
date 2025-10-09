package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    private String status; // PENDING, APPROVED, PLAYING, ELIMINATED
}
