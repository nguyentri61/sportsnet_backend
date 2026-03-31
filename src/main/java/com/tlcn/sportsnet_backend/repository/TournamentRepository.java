package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Tournament;
import com.tlcn.sportsnet_backend.enums.TournamentParticipationTypeEnum;
import com.tlcn.sportsnet_backend.enums.TournamentStatus;
import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, String> {
    @NonNull
    @EntityGraph(attributePaths = {
            "categories",
            "facility"
    })
    Page<Tournament> findAllByStatusNot(Pageable pageable, TournamentStatus status);

    // Filter by status + participationType
    @NonNull
    @EntityGraph(attributePaths = {
            "categories",
            "facility"
    })
    @Query("SELECT t FROM Tournament t WHERE t.status != :status " +
           "AND (:participationType IS NULL OR t.participationType = :participationType)")
    Page<Tournament> findAllByStatusNotAndParticipationType(
            Pageable pageable,
            @Param("status") TournamentStatus status,
            @Param("participationType") TournamentParticipationTypeEnum participationType
    );

    @NonNull
    Page<Tournament> findAll(Pageable pageable);

    Optional<Tournament> findBySlug(String slug);
    @Query("SELECT t FROM Tournament t JOIN t.categories c WHERE c.id = :categoryId")
    Optional<Tournament> findByCategoryId(@Param("categoryId") String categoryId);
    @EntityGraph(attributePaths = {})
    @Query("SELECT e FROM Tournament e WHERE e.status NOT IN :statuses")
    List<Tournament> findAllByStatusNot(@Param("statuses") List<TournamentStatus> excludedStatuses);

    // Fetch tournament by ID for Club Tournament
    @Query("SELECT t FROM Tournament t WHERE t.id = :id")
    Optional<Tournament> findByIdForClubTournament(@Param("id") String id);
}
