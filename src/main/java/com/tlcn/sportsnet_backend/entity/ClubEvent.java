package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "club_events")
@Getter
@Setter
@ToString(exclude = {
        "participants",
        "categories",
        "clubEventRatings"
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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

    @Min(0)
    @Max(5)
    double minLevel;

    @Min(0)
    @Max(5)
    double maxLevel;
    // Các hạng mục (Đơn nam, Đơn nữ, Đôi nam, ...)
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    Set<BadmintonCategoryEnum> categories;

    @Enumerated(EnumType.STRING)
    EventStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    Facility facility;

    // CLB tổ chức
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    Club club;

    @OneToMany(mappedBy = "clubEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClubEventParticipant> participants = new HashSet<>();

    @OneToMany(mappedBy = "clubEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClubEventRating> clubEventRatings = new HashSet<>();


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