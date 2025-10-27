package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.ClubInvitation;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubInvitationRepository extends JpaRepository<ClubInvitation, String> {
    List<ClubInvitation> findAllByReceiver_id(String receiver_id);
    Optional<ClubInvitation> findByReceiver_IdAndClub_Id(String receiver_id, String club_id);
    Optional<ClubInvitation> findByReceiver_IdAndClub_IdAndStatus(String receiver_id, String club_id, InvitationStatusEnum status);
}
