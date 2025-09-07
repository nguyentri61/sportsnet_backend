package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.ApiResponse;
import com.tlcn.sportsnet_backend.dto.club_event.ClubEventCreateRequest;
import com.tlcn.sportsnet_backend.dto.club_event.ClubEventUpdateRequest;
import com.tlcn.sportsnet_backend.service.ClubEventParticipantService;
import com.tlcn.sportsnet_backend.service.ClubEventService;
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
@RequestMapping("/api/club-event")
public class ClubEventController {
    private final ClubEventService clubEventService;
    private final FileStorageService fileStorageService;
    private final ClubEventParticipantService clubEventParticipantService;

    @GetMapping("/{slug}")
    public ResponseEntity<?> getEventClubInfo(@PathVariable String slug) {
        return ResponseEntity.ok(clubEventService.getEventClubInfo(slug));
    }

    @GetMapping("/all/{clubSlug}")
    public ResponseEntity<?> getAllEventClub(
            @PathVariable String clubSlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clubEventService.getAllEventsByClubId(clubSlug, page, size));
    }

    @GetMapping("/all/my_clubs")
    public ResponseEntity<?> getAllMyClubEventClub(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clubEventService.getAllMyClubEventClub(page, size));
    }

    @GetMapping("/all/joined")
    public ResponseEntity<?> getAllMyJoinedClubEvent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clubEventService.getAllMyJoinedClubEvents(page, size));
    }

    @GetMapping("/all/public")
    public ResponseEntity<?> getAllPublicEventClub(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clubEventService.getAllPublicEventClub(page, size));
    }
    @PostMapping
    public ResponseEntity<?> createClubEvent(@RequestBody ClubEventCreateRequest request) {
        return ResponseEntity.ok(clubEventService.createClubEvent(request));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateClubEvent(@RequestBody ClubEventUpdateRequest request) {
        return ResponseEntity.ok(clubEventService.updateClubEvent(request));
    }


    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body("No files uploaded");
        }

        List<String> uploadedFiles = files.stream()
                .filter(f -> !f.isEmpty())
                .map(f -> fileStorageService.storeFile(f, "/club/events"))
                .toList();

        if (uploadedFiles.size() == 1) {
            // Trả về String nếu chỉ có 1 file
            return ResponseEntity.ok()
                    .body(ApiResponse.success(Map.of("fileName", uploadedFiles.getFirst())));
        } else {
            // Trả về List<String> nếu nhiều file
            return ResponseEntity.ok()
                    .body(ApiResponse.success(Map.of("fileNames", uploadedFiles)));
        }
    }

    @PostMapping("/join/{id}")
    public ResponseEntity<?> joinClubEvent(@PathVariable String id) {
        return ResponseEntity.ok(clubEventParticipantService.joinClubEvent(id));
    }

    @GetMapping("/all-participant/{id}")
    public ResponseEntity<?> getAllParticipantClubEvent(@PathVariable String id,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clubEventParticipantService.getAllParticipantClubEvent(id, page, size));
    }


}
