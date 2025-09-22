package com.tlcn.sportsnet_backend.dto.player_rating;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerRatingResponse {
    private String id;
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
    String slug;
}
