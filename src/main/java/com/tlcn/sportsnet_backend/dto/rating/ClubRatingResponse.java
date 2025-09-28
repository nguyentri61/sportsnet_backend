package com.tlcn.sportsnet_backend.dto.rating;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubRatingResponse {
    private Long totalReviews;     // tổng số lượt đánh giá
    private Double averageRating;  // trung bình
    private Long oneStar;
    private Long twoStars;
    private Long threeStars;
    private Long fourStars;
    private Long fiveStars;
    List<ClubEventRatingResponse> clubEventRatingResponses = new ArrayList<>();
    boolean isClubMember;
}
