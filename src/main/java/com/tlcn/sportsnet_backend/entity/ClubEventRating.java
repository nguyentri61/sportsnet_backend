package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "club_event_ratings")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventRating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_event_id", nullable = false)
    private ClubEvent clubEvent; // hoạt động được đánh giá

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account; // người đánh giá

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String comment; // nhận xét

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant createdAt;

    @Column(columnDefinition = "TEXT")
    private String replyComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
    Instant replyAt;

    @Column(nullable = false)
    private boolean clubMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_by") // Ai trả lời (thường là chủ CLB)
    private Account replyBy;

    @PrePersist
    public void handleBeforeCreate(){
        createdAt = Instant.now();
    }
}
