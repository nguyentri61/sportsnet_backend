package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.service.PlayerTournamentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class TournamentMatchController {

    private final PlayerTournamentHistoryService historyService;

    @PostMapping("/{matchId}/finish")
    public ResponseEntity<?> finishMatch(@PathVariable String matchId) {
        return ResponseEntity.ok(historyService.finishMatchAndSaveHistory(matchId));
    }
}
