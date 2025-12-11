package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.TournamentFormat;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournament_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Enumerated(EnumType.STRING)
    BadmintonCategoryEnum category;

    Double minLevel;
    Double maxLevel;
    Integer maxParticipants;

    Double registrationFee; // Lệ phí tham gia

    @Column(columnDefinition = "TEXT")
    String description; // Mô tả hạng mục

    @ElementCollection
    @CollectionTable(
            name = "tournament_category_rules",
            joinColumns = @JoinColumn(name = "category_id")
    )
    @Column(name = "rule", columnDefinition = "TEXT")
    List<String> rules; // Thể lệ thi đấu

    String firstPrize;

    String secondPrize;

    String thirdPrize;

    @Enumerated(EnumType.STRING)
    TournamentFormat format; // Loại trực tiếp, vòng tròn, v.v.

    LocalDateTime registrationDeadline;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    Tournament tournament;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<TournamentParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<TournamentTeam> teams = new ArrayList<>();;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<TournamentResult> results = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<TournamentMatch> matches = new ArrayList<>();

    String slug;

    @PrePersist
    public void handleBeforeCreate(){

        if (this.slug == null || this.slug.isBlank()) {
            String slug = SlugUtil.toSlug(this.category.getLabel());
            String randomSuffix = SlugUtil.randomString(8);
            this.slug = slug + "-" + randomSuffix;
        }
    }

}
