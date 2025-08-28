package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.beans.Visibility;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, String> {
    Page<Club> findAllByVisibility(ClubVisibilityEnum visibility, Pageable pageable);
}
