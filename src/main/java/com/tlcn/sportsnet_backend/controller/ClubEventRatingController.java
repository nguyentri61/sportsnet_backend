package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.club_event.ClubEventCreateRequest;
import com.tlcn.sportsnet_backend.dto.rating.ClubEventRatingCreateRequest;
import com.tlcn.sportsnet_backend.dto.rating.ReplyRatingCreateRequest;
import com.tlcn.sportsnet_backend.service.ClubEventRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/club-event-rating")
public class ClubEventRatingController {

    private final ClubEventRatingService clubEventRatingService;
    @PostMapping
    public ResponseEntity<?> createClubEventRating(@RequestBody ClubEventRatingCreateRequest request) {
        return ResponseEntity.ok(clubEventRatingService.createClubEvent(request));
    }

    @PostMapping("/reply")
    public ResponseEntity<?> createReplyClubEventRating(@RequestBody ReplyRatingCreateRequest request) {
        return ResponseEntity.ok(clubEventRatingService.createReplyRatingClubEvent(request));
    }
    @GetMapping("/{clubId}")
    public ResponseEntity<?> getAllClubEventRating( @PathVariable String clubId) {
        return ResponseEntity.ok(clubEventRatingService.getAllByClubId(clubId));
    }

    @GetMapping("/own/{clubId}")
    public ResponseEntity<?> getOwnClubEventRating( @PathVariable String clubId) {
        return ResponseEntity.ok(clubEventRatingService.getOwnClubEventRatingByClubId(clubId));
    }
}
