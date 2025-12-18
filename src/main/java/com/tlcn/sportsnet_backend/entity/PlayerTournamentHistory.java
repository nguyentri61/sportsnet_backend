package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player_tournament_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerTournamentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Account player;

    // Hạng mục của giải
    @ManyToOne
    @JoinColumn(name = "category_id")
    private TournamentCategory category;

    private String matchId;

    // Nếu là đấu đôi → lưu teamId
    private String teamId;

    private boolean isDouble;

    // Hạng cuối cùng
    private Integer finalRanking;

    private String prize;

    private Double oldLevel;
    private Double newLevel;

    @OneToMany(
            mappedBy = "playerHistory",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RoundHistory> rounds = new ArrayList<>();

    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
    }
}
