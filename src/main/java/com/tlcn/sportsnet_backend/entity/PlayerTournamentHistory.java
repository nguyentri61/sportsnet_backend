package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Column(name = "player_id")
    private String playerId;

    // Hạng mục của giải
    @Column(name = "category_id")
    private String categoryId;

    // Nếu là đấu đôi → lưu teamId
    private String teamId;

    private boolean isDouble;

    // Hạng cuối cùng
    private Integer finalRanking;

    private String prize;

    private Double oldLevel;
    private Double newLevel;

//    @OneToMany(
//            mappedBy = "playerHistory",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true,
//            fetch = FetchType.LAZY
//    )
//    @OrderBy("round ASC")
//    private List<RoundHistory> rounds = new ArrayList<>();

    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
    }
}
