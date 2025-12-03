package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    TournamentCategory category;

    // Round 1, Round 2, Quarterfinal, Semifinal, Final
    @Column(nullable = false)
    private Integer round;

    // matchIndex trong mỗi round (1,2,3,...)
    @Column(nullable = false)
    private Integer matchIndex;

    @ManyToOne
    @JoinColumn(name = "player1_id")
    TournamentParticipant player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    TournamentParticipant player2;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    TournamentParticipant winner;

    // Điểm số
    @ElementCollection
    List<Integer> setScoreP1;

    @ElementCollection
    List<Integer> setScoreP2;

    @Enumerated(EnumType.STRING)
    MatchStatus status;

    LocalDateTime startTime;
    LocalDateTime endTime;
    String note;
}
