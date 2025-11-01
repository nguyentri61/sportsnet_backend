package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.service.ClubEventCancellationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event-cancellations")
@RequiredArgsConstructor
public class ClubEventCancellationController {
    private final ClubEventCancellationService clubEventCancellationService;

    @PostMapping("/{participantId}/request")
    public ResponseEntity<?> requestCancellation(
            @PathVariable String participantId,
            @RequestParam String reason) {
        clubEventCancellationService.requestLateCancel(participantId, reason);
        return ResponseEntity.ok("Đã gửi yêu cầu hủy thành công");
    }

    @PostMapping("/{cancellationId}/review")
    public ResponseEntity<?> reviewCancellation(
            @PathVariable String cancellationId,
            @RequestParam boolean approve) {
        clubEventCancellationService.reviewCancellation(cancellationId, approve);
        return ResponseEntity.ok(approve ? "Đã phê duyệt hủy" : "Đã từ chối hủy");
    }
}
