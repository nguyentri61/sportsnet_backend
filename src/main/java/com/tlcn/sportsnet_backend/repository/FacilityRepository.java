package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, String> {
}
