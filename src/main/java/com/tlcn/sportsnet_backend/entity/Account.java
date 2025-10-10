package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    String id;

    @Column(unique=true)
    String email;

    @Column(nullable=false)
    String password;

    boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant createdAt;

    Instant updatedAt;

    String createdBy;

    String updatedBy;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubEventRating> clubEventRatings = new ArrayList<>();

    @PrePersist
    public void handleBeforeCreate(){
        createdAt = Instant.now();
        enabled = true;
        createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
    }

    @PreUpdate
    public void handleBeforeUpdate(){
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
    }

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    UserInfo userInfo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    Set<Role> roles = new HashSet<>();

    // Social
    @OneToMany(mappedBy = "owner")
    Set<Club> ownedClubs = new HashSet<>();

    @OneToMany(mappedBy = "requester")
    Set<Friendship> sentFriendships = new HashSet<>();

    @OneToMany(mappedBy = "receiver")
    Set<Friendship> receivedFriendships = new HashSet<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Post> posts = new HashSet<>();

    // Messaging
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Message> sentMessages = new HashSet<>();

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PlayerRating playerRating;

    private int totalParticipatedEvents =0;
    @Column(nullable = false)
    int reputationScore = 100;

    // Lịch sử thay đổi điểm uy tín
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReputationHistory> reputationHistories = new ArrayList<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    List<TournamentParticipant> tournamentParticipants = new ArrayList<>();

    // Các đội mà người này là player1
    @OneToMany(mappedBy = "player1", fetch = FetchType.LAZY)
    private List<TournamentTeam> teamsAsPlayer1 = new ArrayList<>();

    // Các đội mà người này là player2
    @OneToMany(mappedBy = "player2", fetch = FetchType.LAZY)
    private List<TournamentTeam> teamsAsPlayer2 = new ArrayList<>();
}
