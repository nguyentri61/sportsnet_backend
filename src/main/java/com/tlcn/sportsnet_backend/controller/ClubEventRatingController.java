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
    @GetMapping("/{clubEventId}")
    public ResponseEntity<?> getAllClubEventRating( @PathVariable String clubEventId) {
        return ResponseEntity.ok(clubEventRatingService.getAllByClubEventId(clubEventId));
    }

    @GetMapping("/own/{clubEventId}")
    public ResponseEntity<?> getOwnClubEventRating( @PathVariable String clubEventId) {
        return ResponseEntity.ok(clubEventRatingService.getOwnClubEventRatingByClubId(clubEventId));
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<?> getAllClubEventRatingByClubId(@PathVariable String clubId) {
        return ResponseEntity.ok(clubEventRatingService.getAllByClubId(clubId));
    }

    @GetMapping("/club/{clubId}/more")
    public ResponseEntity<?> getMoreClubEventRatingByClubId(@PathVariable String clubId,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(clubEventRatingService.getMoreByClubId(clubId, page , size));
    }
}
