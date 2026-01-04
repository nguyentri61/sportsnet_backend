package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "round_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "playerHistory")
public class RoundHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "player_history_id")
    private String playerHistoryId;

    private Integer round;

    private String opponentId;
    private String opponentName;

    private boolean won;

    @ElementCollection
    private List<Integer> scoreP1;

    @ElementCollection
    private List<Integer> scoreP2;
}
