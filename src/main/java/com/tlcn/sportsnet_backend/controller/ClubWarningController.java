package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.clubWarning.ClubWarningGetRequest;
import com.tlcn.sportsnet_backend.dto.clubWarning.ClubWarningRequest;
import com.tlcn.sportsnet_backend.dto.club_event.ClubEventCreateRequest;
import com.tlcn.sportsnet_backend.service.ClubWarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/club_warning")
public class ClubWarningController {

    private final ClubWarningService clubWarningService;

    @PostMapping
    public ResponseEntity<?> createClubWarning(@RequestBody ClubWarningRequest request) {
        return ResponseEntity.ok(clubWarningService.createClubWarning(request));
    }

    @GetMapping("/{clubId}/{accountId}")
    public ResponseEntity<?> getAllClubWarning(@PathVariable String clubId, @PathVariable String accountId) {
        return ResponseEntity.ok(clubWarningService.getAllClubWarnings(clubId, accountId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> revokeClubWarning(@PathVariable String id) {
        return ResponseEntity.ok(clubWarningService.revokeClubWarning(id));
    }

}
