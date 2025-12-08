package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "tournament_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    Integer ranking;
    String prize;

    // Nếu là đơn → participant != null
    @ManyToOne
    @JoinColumn(name = "participant_id")
    TournamentParticipant participant;

    // Nếu là đôi → team != null
    @ManyToOne
    @JoinColumn(name = "team_id")
    TournamentTeam team;

    @ManyToOne
    @JoinColumn(name = "category_id")
    TournamentCategory category;
}
