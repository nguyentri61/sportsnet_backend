package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.facility.FacilityCreateRequest;
import com.tlcn.sportsnet_backend.service.FaclityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facilities")
public class FacilityController {
    private final FaclityService faclityService;

    @PostMapping
    public ResponseEntity<?> addFacility(@RequestBody FacilityCreateRequest request) {
        return ResponseEntity.ok(faclityService.createFacility(request));
    }
}
