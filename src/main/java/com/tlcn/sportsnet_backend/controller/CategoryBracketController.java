package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.bracket.UpdateMatchResultRequest;
import com.tlcn.sportsnet_backend.service.TournamentBracketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bracket")
public class CategoryBracketController {
    private final TournamentBracketService bracketService;

    @GetMapping("/{categoryId}/bracket-tree")
    public ResponseEntity<?> getBracketTree(@PathVariable String categoryId) {
        return ResponseEntity.ok(bracketService.getBracketTree(categoryId));
    }

    @PostMapping("/{categoryId}/generate-bracket")
    public ResponseEntity<?> generateBracket(@PathVariable String categoryId) {
        return ResponseEntity.ok(bracketService.generateBracket(categoryId));
    }

    @PostMapping("/match/{matchId}/update-result")
    public ResponseEntity<?> updateResult(@PathVariable String matchId,
                                          @RequestBody UpdateMatchResultRequest req) {
        return ResponseEntity.ok(bracketService.updateMatchResult(matchId, req));
    }
}
