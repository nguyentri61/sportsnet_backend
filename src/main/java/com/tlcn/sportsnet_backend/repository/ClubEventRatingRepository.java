package com.tlcn.sportsnet_backend.repository;


import com.tlcn.sportsnet_backend.entity.ClubEventRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubEventRatingRepository extends JpaRepository<ClubEventRating, String> {
    List<ClubEventRating> findByClubEventIdOrderByCreatedAtDesc(String clubEventId);
    Optional<ClubEventRating> findByClubEventIdAndAccountId(String clubEventId, String accountId);
    List<ClubEventRating> findAllByClubEvent_Club_IdOrderByCreatedAtDesc(String clubId, Pageable pageable);
    Page<ClubEventRating> findAllByClubEvent_Club_IdOrderByCreatedAtDesc(Pageable pageable, String clubId);
    // Tổng số lượt đánh giá
    Long countByClubEvent_Club_Id(String clubId);

    // Điểm trung bình có trọng số (30% thành viên CLB, 70% khách vãng lai)
    @Query(
            "SELECT " +
                    "COALESCE((SELECT AVG(r1.rating) FROM ClubEventRating r1 " +
                    "WHERE r1.clubEvent.club.id = :clubId AND r1.clubMember = true), 0) * 0.3 " +
                    "+ COALESCE((SELECT AVG(r2.rating) FROM ClubEventRating r2 " +
                    "WHERE r2.clubEvent.club.id = :clubId AND r2.clubMember = false), 0) * 0.7 " +
                    "FROM Club c " +
                    "WHERE c.id = :clubId"
    )
    Double getWeightedAverageRatingByClubId(@Param("clubId") String clubId);

    // Đếm số lượng theo từng sao (1 → 5)
    @Query("SELECT r.rating, COUNT(r) " +
            "FROM ClubEventRating r " +
            "WHERE r.clubEvent.club.id = :clubId " +
            "GROUP BY r.rating")
    List<Object[]> getRatingCountsByClubId(@Param("clubId") String clubId);


}
