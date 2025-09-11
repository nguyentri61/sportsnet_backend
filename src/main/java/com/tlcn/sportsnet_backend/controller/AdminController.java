package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.enums.ClubStatusEnum;
import com.tlcn.sportsnet_backend.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ClubService clubService;

    @GetMapping("/clubs/all")
    public ResponseEntity<?> getAllClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clubService.getAllClubs(page, size));
    }


    @PutMapping("/clubs/{id}/status")
    public ResponseEntity<?> updateClubStatus(@PathVariable String id, @RequestParam ClubStatusEnum newStatus) {
        clubService.updateClubStatus(id, newStatus);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clubs/{id}")
    public ResponseEntity<?> deleteClub(@PathVariable String id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }
}
