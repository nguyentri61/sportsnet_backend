package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.absentReason.AbsentReasonRequest;
import com.tlcn.sportsnet_backend.dto.player_rating.PlayerRatingCreateRequest;
import com.tlcn.sportsnet_backend.service.AbsentReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/absent-reason")
@RequiredArgsConstructor
public class AbsentReasonController {
    private final AbsentReasonService absentReasonService;
    @PostMapping
    public ResponseEntity<?> createPlayerRating(@RequestBody AbsentReasonRequest request) {
        return ResponseEntity.ok(absentReasonService.createAbsentReason(request));
    }

    @GetMapping("/{idPart}")
    public ResponseEntity<?> getAllAbsentReason(@PathVariable String idPart) {
        return ResponseEntity.ok(absentReasonService.getByIdPart(idPart));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveAbsentReason(@PathVariable String id) {
        return ResponseEntity.ok(absentReasonService.approveAbsentReason(id));
    }
    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectAbsentReason(@PathVariable String id) {
        return ResponseEntity.ok(absentReasonService.rejectAbsentReason(id));
    }

}
