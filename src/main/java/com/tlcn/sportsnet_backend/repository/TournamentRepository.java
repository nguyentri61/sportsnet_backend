package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Tournament;
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

import java.time.LocalDateTime;
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

    @NonNull
    Page<Tournament> findAll(Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {
            "categories",
            "facility"
    })
    @Query("""
            SELECT t
            FROM Tournament t
            WHERE t.status <> :excludedStatus
            AND (:content IS NULL OR TRIM(:content) = ''
                 OR LOWER(t.name) LIKE LOWER(CONCAT('%', :content, '%'))
                 OR LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :content, '%')))
            AND (:organizationDateFrom IS NULL OR t.startDate >= :organizationDateFrom)
            AND (:organizationDateTo IS NULL OR t.startDate <= :organizationDateTo)
            """)
    Page<Tournament> searchTournaments(
            Pageable pageable,
            @Param("excludedStatus") TournamentStatus excludedStatus,
            @Param("content") String content,
            @Param("organizationDateFrom") LocalDateTime organizationDateFrom,
            @Param("organizationDateTo") LocalDateTime organizationDateTo
    );

    Optional<Tournament> findBySlug(String slug);
    @Query("SELECT t FROM Tournament t JOIN t.categories c WHERE c.id = :categoryId")
    Optional<Tournament> findByCategoryId(@Param("categoryId") String categoryId);
    @EntityGraph(attributePaths = {})
    @Query("SELECT e FROM Tournament e WHERE e.status NOT IN :statuses")
    List<Tournament> findAllByStatusNot(@Param("statuses") List<TournamentStatus> excludedStatuses);

}
