package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.entity.ClubMemberNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMemberNoteRepository extends JpaRepository<ClubMemberNote, String> {
    List<ClubMemberNote> findAllByClubMemberOrderByCreatedAtDesc(ClubMember clubMember);
}
