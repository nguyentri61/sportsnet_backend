package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournament_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    private Integer ranking;
    private String prize;

    // Nếu là đơn → participant != null
    @ManyToOne
    @JoinColumn(name = "participant_id")
    private TournamentParticipant participant;

    // Nếu là đôi → team != null
    @ManyToOne
    @JoinColumn(name = "team_id")
    private TournamentTeam team;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private TournamentCategory category;
}
