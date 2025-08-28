package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.ApiResponse;
import com.tlcn.sportsnet_backend.dto.club.ClubCreateRequest;
import com.tlcn.sportsnet_backend.service.ClubService;
import com.tlcn.sportsnet_backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String uploadedFile = fileStorageService.storeFile(file, "/club/logo");

        return ResponseEntity.ok()
                .body(ApiResponse.success(Map.of("fileName", uploadedFile)));
    }
}
