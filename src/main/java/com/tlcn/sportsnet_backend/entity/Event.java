package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlcn.sportsnet_backend.enums.*;
import com.tlcn.sportsnet_backend.model.BadmintonEventFormat;
import com.tlcn.sportsnet_backend.model.BadmintonSportRule;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.time.LocalDate;
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
@Table(name = "events")
public class Event {
    @Id
    String id;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "MEDIUMTEXT")
    String description;

    String image;

    LocalDate date;
    String location;

    LocalDateTime startTime;
    LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    EventTypeEnum eventType;

    @Enumerated(EnumType.STRING)
    BadmintonCategoryEnum badmintonCategory; // Đơn/Đôi các loại

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    BadmintonEventFormat eventFormat; // mode, rounds, thirdPlaceMatch

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    BadmintonSportRule sportRule; // bestOf, pointsPerSet, winBy, maxPoints

    @Enumerated(EnumType.STRING)
    EventStatusEnum status;

    @ManyToOne @JoinColumn(name = "club_id")
    Club club;

    @ManyToOne @JoinColumn(name = "facility_id")
    Facility facility;

    @ManyToOne @JoinColumn(name = "organizer_id", nullable = false)
    Account organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Team> teams = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Participant> participants = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Match> matches = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Payment> payments = new HashSet<>();


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    @PrePersist
    public void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            String slug = SlugUtil.toSlug(this.title);
            String randomSuffix = SlugUtil.randomString(8);
            this.id = slug + "-" + randomSuffix;
        }
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        status = status == null ? EventStatusEnum.OPEN : status;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
