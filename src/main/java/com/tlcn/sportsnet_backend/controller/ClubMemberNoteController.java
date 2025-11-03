package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.member_note.ClubMemberNoteRequest;
import com.tlcn.sportsnet_backend.service.ClubMemberNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/club-member-notes")
@RequiredArgsConstructor
public class ClubMemberNoteController {
    private final ClubMemberNoteService clubMemberNoteService;

    @PostMapping
    public ResponseEntity<?> createClubMemberNote(@RequestBody ClubMemberNoteRequest request) {
        return ResponseEntity.ok(clubMemberNoteService.createClubMemberNote(request));
    }

    @GetMapping("/{clubId}/{accountId}")
    public ResponseEntity<?> getAllClubMemberNotes(
            @PathVariable String clubId,
            @PathVariable String accountId
    ) {
        return ResponseEntity.ok(clubMemberNoteService.getAllClubMemberNotes(clubId, accountId));
    }
}
