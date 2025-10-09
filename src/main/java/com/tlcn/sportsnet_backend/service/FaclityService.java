package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.facility.FacilityCreateRequest;
import com.tlcn.sportsnet_backend.entity.Facility;
import com.tlcn.sportsnet_backend.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaclityService {
    private final FacilityRepository facilityRepository;

    public Facility createFacility(FacilityCreateRequest request) {
        Facility facility = Facility.builder()
                .name(request.getName())
                .address(request.getAddress())
                .district(request.getDistrict())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .image(request.getImage())
                .build();
        return facilityRepository.save(facility);
    }
}
