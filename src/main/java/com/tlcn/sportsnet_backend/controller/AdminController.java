package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.enums.ClubStatusEnum;
import com.tlcn.sportsnet_backend.service.AdminService;
import com.tlcn.sportsnet_backend.service.ClubEventService;
import com.tlcn.sportsnet_backend.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    @GetMapping("/clubs/all")
    public ResponseEntity<?> getAllClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllClubs(page, size));
    }


    @PutMapping("/clubs/{id}/status")
    public ResponseEntity<?> updateClubStatus(@PathVariable String id, @RequestParam ClubStatusEnum newStatus) {
        adminService.updateClubStatus(id, newStatus);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clubs/{id}")
    public ResponseEntity<?> deleteClub(@PathVariable String id) {
        adminService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/all")
    public ResponseEntity<?> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllAdminEvent(page, size));
    }

    @GetMapping("users/all")
    public ResponseEntity<?> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllAccounts(page, size));
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<?> updateAccountStatus(@PathVariable String id) {
        adminService.updateAccountStatus(id);
        return ResponseEntity.noContent().build();
    }
}
