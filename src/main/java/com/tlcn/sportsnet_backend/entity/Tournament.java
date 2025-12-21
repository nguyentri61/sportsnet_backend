package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlcn.sportsnet_backend.enums.TournamentStatus;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tournaments")
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    @Column(columnDefinition = "TEXT")
    String description;

    String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    Facility facility;

    LocalDateTime startDate;
    LocalDateTime endDate;
    LocalDateTime registrationStartDate;
    LocalDateTime registrationEndDate;
    String logoUrl;
    String bannerUrl;

    @Enumerated(EnumType.STRING)
    TournamentStatus status = TournamentStatus.UPCOMING;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant createdAt;
    Instant updatedAt;

    String createdBy;

    String updatedBy;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String rules;

    String slug;
    @PrePersist
    public void handleBeforeCreate(){
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (this.slug == null || this.slug.isBlank()) {
            String slug = SlugUtil.toSlug(this.name);
            String randomSuffix = SlugUtil.randomString(8);
            this.slug = slug + "-" + randomSuffix;
        }
    }

    @PreUpdate
    public void handleBeforeUpdate(){
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (this.slug == null || this.slug.isBlank()) {
            String slug = SlugUtil.toSlug(this.name);
            String randomSuffix = SlugUtil.randomString(8);
            this.slug = slug + "-" + randomSuffix;
        }
    }

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    List<TournamentCategory> categories;
}
