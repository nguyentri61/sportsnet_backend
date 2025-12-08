package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "round_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer round;

    private String opponentId;
    private String opponentName;

    private boolean won;

    @ElementCollection
    private List<Integer> scoreP1;

    @ElementCollection
    private List<Integer> scoreP2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_history_id")
    private PlayerTournamentHistory playerHistory;
}
