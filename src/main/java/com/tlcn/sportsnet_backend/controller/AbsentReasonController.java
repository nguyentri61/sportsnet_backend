package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.absentReason.AbsentReasonRequest;
import com.tlcn.sportsnet_backend.dto.player_rating.PlayerRatingCreateRequest;
import com.tlcn.sportsnet_backend.service.AbsentReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/absent-reason")
@RequiredArgsConstructor
public class AbsentReasonController {
    private final AbsentReasonService absentReasonService;
    @PostMapping
    public ResponseEntity<?> createPlayerRating(@RequestBody AbsentReasonRequest request) {
        return ResponseEntity.ok(absentReasonService.createAbsentReason(request));
    }



}
