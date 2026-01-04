package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.service.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournament-result")
@RequiredArgsConstructor
public class TournamentResultController {
    private final TournamentResultService resultService;

    @GetMapping("/{tournamentId}/results")
    public ResponseEntity<?> getTournamentResults(
            @PathVariable String tournamentId
    ) {
        return ResponseEntity.ok(
                resultService.getTournamentResults(tournamentId)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getCategoryResult(
            @PathVariable String categoryId
    ) {
        return ResponseEntity.ok(
                resultService.getCategoryResult(categoryId)
        );
    }


    @PostMapping("/{tournamentId}/results/generate")
    public ResponseEntity<?> generateResults(@PathVariable String tournamentId) {
        resultService.generateResultsByTournament(tournamentId);
        return ResponseEntity.ok("Tournament results generated successfully");
    }
}
