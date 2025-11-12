package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import com.tlcn.sportsnet_backend.service.TournamentParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournament-participants")
@RequiredArgsConstructor
public class TournamentParticipantController {
    private final TournamentParticipantService tournamentParticipantService;

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getAllParticipants(
            @PathVariable String categoryId,
            @RequestParam(required = false) List<TournamentParticipantEnum> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(tournamentParticipantService.getAllParticipants(categoryId, status, page, size));
    }

    @PutMapping("/{participantId}/approve")
    public ResponseEntity<?> approveParticipant(@PathVariable String participantId) {
        tournamentParticipantService.updateParticipantStatus(participantId, TournamentParticipantEnum.APPROVED);
        return ResponseEntity.ok("Participant approved successfully.");
    }

    @PutMapping("/{participantId}/reject")
    public ResponseEntity<?> rejectParticipant(@PathVariable String participantId) {
        tournamentParticipantService.updateParticipantStatus(participantId, TournamentParticipantEnum.REJECTED);
        return ResponseEntity.ok("Participant rejected successfully.");
    }
}
