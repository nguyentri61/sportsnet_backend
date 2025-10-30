package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.club.ClubCreateRequest;
import com.tlcn.sportsnet_backend.dto.clubInvitation.ClubInvitationRequest;
import com.tlcn.sportsnet_backend.dto.clubInvitation.ClubInvitationUpdateStatus;
import com.tlcn.sportsnet_backend.service.ClubInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/club_invitation")
public class ClubInvitationController {

    private final  ClubInvitationService clubInvitationService;
    @PostMapping
    public ResponseEntity<?> createClubInvitation(@RequestBody ClubInvitationRequest request) {
        return ResponseEntity.ok(clubInvitationService.createClubInvitation(request));
    }

    @PutMapping
    public ResponseEntity<?> updateClubInvitation(@RequestBody ClubInvitationUpdateStatus request, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(clubInvitationService.updateStatusClubInvitation(request, reason));
    }

    @GetMapping
    public ResponseEntity<?> getAllClubInvitation() {
        return ResponseEntity.ok(clubInvitationService.getClubByUser());
    }
}
