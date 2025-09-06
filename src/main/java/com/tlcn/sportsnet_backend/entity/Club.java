package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "clubs")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(unique = true, nullable = false)
    private String slug;
    String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    String description;

    String location;

    String logoUrl;

    @Enumerated(EnumType.STRING)
    ClubVisibilityEnum visibility;

    @Builder.Default
    boolean active = false;

    int maxMembers;

    @ElementCollection
    @CollectionTable(
            name = "club_tags",
            joinColumns = @JoinColumn(name = "club_id")
    )
    @Column(name = "tag")
    Set<String> tags = new HashSet<>(); //mô tả nhanh về CLB, ví dụ: “cầu lông nghiệp dư”, “thi đấu giải”.

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    Account owner;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ClubMember> members = new HashSet<>();

    @OneToMany(mappedBy = "club")
    Set<ClubEvent> events = new HashSet<>();

    @OneToMany(mappedBy = "club")
    Set<Post> posts = new HashSet<>();

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
}
