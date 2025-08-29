package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.ApiResponse;
import com.tlcn.sportsnet_backend.dto.club.ClubCreateRequest;
import com.tlcn.sportsnet_backend.payload.exception.CustomUnauthorizedException;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.service.ClubMemberService;
import com.tlcn.sportsnet_backend.service.ClubService;
import com.tlcn.sportsnet_backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class ClubController {
    private final ClubService clubService;
    private final FileStorageService fileStorageService;
    private final ClubMemberService clubMemberService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getClubInformation(@PathVariable String id) {
        return ResponseEntity.ok(clubService.getClubInformation(id));
    }

    @GetMapping("/all_public")
    public ResponseEntity<?> getAllPublicClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clubService.getAllClubPublic(page, size));
    }

    @GetMapping("/my_clubs/all")
    public ResponseEntity<?> getAllMyClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clubService.getAllMyClub(page, size));
    }

    @PostMapping
    public ResponseEntity<?> createClub(@RequestBody ClubCreateRequest request) {
        return ResponseEntity.ok(clubService.createClub(request));
    }

    @PutMapping("/active/{id}")
    public ResponseEntity<?> activateClub(@PathVariable String id) {
        clubService.activateClub(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String uploadedFile = fileStorageService.storeFile(file, "/club/logo");

        return ResponseEntity.ok()
                .body(ApiResponse.success(Map.of("fileName", uploadedFile)));
    }

    @PostMapping("/{clubId}/join")
    public ResponseEntity<?> joinClub(@PathVariable String clubId) {
        return ResponseEntity.ok(clubMemberService.joinClub(clubId));
    }

    @PostMapping("/{clubId}/members/{memberId}/approve")
    public ResponseEntity<?> approveMember(
            @PathVariable String clubId,
            @PathVariable String memberId,
            @RequestParam boolean approve) {

        clubMemberService.approveMember(clubId, memberId, approve);
        if (approve) {
            return ResponseEntity.ok("Member approved successfully");
        } else {
            return ResponseEntity.ok("Member rejected successfully");
        }
    }

    @PostMapping("/{clubId}/members/{memberId}/ban")
    public ResponseEntity<?> banMember(@PathVariable String clubId, @PathVariable String memberId) {
        clubMemberService.banMember(clubId, memberId);
        return ResponseEntity.ok().body("Ban success");
    }
}
