package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.entity.ClubWarning;
import com.tlcn.sportsnet_backend.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubWarningRepository extends JpaRepository<ClubWarning, String> {
    List<ClubWarning> findAllByClubMemberOrderByCreatedAtDesc(ClubMember clubMember);
}
