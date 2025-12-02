package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.tournament_participants.TournamentPartnerInvitationRequest;
import com.tlcn.sportsnet_backend.dto.tournament_participants.TournamentPartnerInvitationUpdate;
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

    @GetMapping("/{categoryId}/double")
    public ResponseEntity<?> getAllTeamParticipants(
            @PathVariable String categoryId,
            @RequestParam(required = false) List<TournamentParticipantEnum> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(tournamentParticipantService.getAllTeamParticipants(categoryId, status, page, size));
    }

    @PutMapping("/{participantId}/approve")
    public ResponseEntity<?> approveParticipant(@PathVariable String participantId) {
        tournamentParticipantService.updateParticipantStatus(participantId, TournamentParticipantEnum.APPROVED);
        return ResponseEntity.ok("Participant approved successfully.");
    }

    @PutMapping("/double/{teamId}/approve")
    public ResponseEntity<?> approveTeam(@PathVariable String teamId) {
        tournamentParticipantService.updateTeamStatus(teamId, TournamentParticipantEnum.APPROVED);
        return ResponseEntity.ok("Participant approved successfully.");
    }

    @PutMapping("/double/{teamId}/reject")
    public ResponseEntity<?> rejectTeam(@PathVariable String teamId) {
        tournamentParticipantService.updateTeamStatus(teamId, TournamentParticipantEnum.REJECTED);
        return ResponseEntity.ok("Participant rejected successfully.");
    }

    @PutMapping("/{participantId}/reject")
    public ResponseEntity<?> rejectParticipant(@PathVariable String participantId) {
        tournamentParticipantService.updateParticipantStatus(participantId, TournamentParticipantEnum.REJECTED);
        return ResponseEntity.ok("Participant rejected successfully.");
    }

    @PostMapping("/invite-partner")
    public ResponseEntity<?> invitePartner(@RequestBody TournamentPartnerInvitationRequest request) {
        tournamentParticipantService.invitePartner(request);
        return ResponseEntity.ok("Participant invited successfully.");
    }

    @PutMapping("/partner/update-status")
    public ResponseEntity<?> updatePartner(@RequestBody TournamentPartnerInvitationUpdate request) {
        tournamentParticipantService.updatePartnerStatus(request);
        return ResponseEntity.ok("Participant updated successfully.");
    }
}
