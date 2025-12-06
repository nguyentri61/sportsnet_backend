package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "player_rating")
public class PlayerRating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account;

    private int experience;
    private int serve;
    private int smash;
    private int clear;
    private int dropShot;
    private int drive;
    private int netShot;
    private int doubles;
    private int defense;
    private int footwork;
    private int stamina;
    private int tactics;

    private double averageTechnicalScore;
    private double overallScore;
    private String skillLevel;

    private Instant createdAt;
    private Instant updatedAt;



    @PrePersist
    public void handleBeforeCreate() {
        calculateScores();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        calculateScores();
        this.updatedAt = Instant.now();
    }

    // --- Hàm tính toán chung ---
    private void calculateScores() {
        calculateAverageTechnicalScore();
        calculateOverallScore();
        this.skillLevel = determineSkillLevel();
    }

    private void calculateAverageTechnicalScore() {
        int total = serve + smash + clear + dropShot + drive + netShot + doubles + defense + footwork;
        this.averageTechnicalScore = total / 9.0;
    }

    private void calculateOverallScore() {
        this.overallScore = 0.3 * experience
                + 0.4 * averageTechnicalScore
                + 0.2 * stamina
                + 0.1 * tactics;
    }

    private String determineSkillLevel() {
        if (overallScore <= 1) return "Mới tập chơi";
        if (overallScore <= 2) return "Cơ bản";
        if (overallScore <= 3) return "Trung bình";
        if (overallScore <= 4) return "Trung bình khá";
        if (overallScore <= 4.5) return "Khá";
        return "Bán chuyên";
    }
}
