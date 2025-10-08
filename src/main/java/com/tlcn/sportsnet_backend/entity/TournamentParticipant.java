package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import jakarta.persistence.*;
import lombok.*;
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

    private TournamentParticipantEnum status;
}
