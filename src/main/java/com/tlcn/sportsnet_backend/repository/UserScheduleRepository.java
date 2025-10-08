package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.UserSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserScheduleRepository extends JpaRepository<UserSchedule, String> {
    UserSchedule findByAccountId(String accountId);
    Page<UserSchedule> findByAccountId(String accountId, Pageable pageable);

    @Query("""
    SELECT COUNT(us) > 0
    FROM UserSchedule us
    WHERE us.account.id = :accountId
      AND (us.status != 'CANCELLED' OR us.status != 'REJECTED')
      AND (
          us.startTime < :eventEndTime
          AND us.endTime > :eventStartTime
      )
""")
    boolean hasConflictWithEventTime(
            @Param("accountId") String accountId,
            @Param("eventStartTime") LocalDateTime eventStartTime,
            @Param("eventEndTime") LocalDateTime eventEndTime
    );
}
