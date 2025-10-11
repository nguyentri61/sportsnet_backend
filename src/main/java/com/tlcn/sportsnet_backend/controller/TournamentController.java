package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tournaments")
public class TournamentController {
    private final TournamentService tournamentService;
    @GetMapping
    public ResponseEntity<?> getAllMyClubEventClub(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(tournamentService.getAllTournament(page, size));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getTournamentBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(tournamentService.getBySlug(slug));
    }
}
