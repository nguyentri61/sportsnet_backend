package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "club_events")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(unique = true, nullable = false)
    private String slug;

    String title;

    @Lob
    String description;

    String image;

    String location;
    @Column(columnDefinition = "TEXT")
    String requirements;

    @Column(name = "start_time")
    LocalDateTime startTime;

    @Column(name = "end_time")
    LocalDateTime endTime;

    BigDecimal fee;

    LocalDateTime deadline;

    @Builder.Default
    boolean openForOutside = false;

    int totalMember;

    int maxClubMembers;   // số lượng thành viên CLB được tham gia
    int maxOutsideMembers;

    // Các hạng mục (Đơn nam, Đơn nữ, Đôi nam, ...)
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    List<BadmintonCategoryEnum> categories;

    @Enumerated(EnumType.STRING)
    EventStatusEnum status;

    // CLB tổ chức
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    Club club;

    @OneToMany(mappedBy = "clubEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubEventParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "clubEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubEventRating> clubEventRatings = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant createdAt;

    Instant updatedAt;

    String createdBy;

    String updatedBy;

    @PrePersist
    public void handleBeforeCreate(){
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (this.slug == null || this.slug.isBlank()) {
            String slug = SlugUtil.toSlug(this.title);
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
            String slug = SlugUtil.toSlug(this.title);
            String randomSuffix = SlugUtil.randomString(8);
            this.slug = slug + "-" + randomSuffix;
        }
    }
}