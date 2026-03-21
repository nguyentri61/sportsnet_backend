package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.entity.ClubTournamentParticipant;
import com.tlcn.sportsnet_backend.entity.ClubTournamentRoster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubTournamentRosterRepository extends JpaRepository<ClubTournamentRoster, String> {
    
    // Lấy tất cả thành viên trong roster của một participant
    List<ClubTournamentRoster> findByClubTournamentParticipant(ClubTournamentParticipant participant);
    
    // Lấy roster theo participant ID
    List<ClubTournamentRoster> findByClubTournamentParticipantId(String participantId);
    
    // Kiểm tra club member đã có trong roster chưa
    boolean existsByClubTournamentParticipantAndClubMember(
            ClubTournamentParticipant participant, 
            ClubMember clubMember
    );
    
    // Tìm roster entry cụ thể
    Optional<ClubTournamentRoster> findByClubTournamentParticipantAndClubMember(
            ClubTournamentParticipant participant,
            ClubMember clubMember
    );
    
    // Đếm số lượng members trong roster
    int countByClubTournamentParticipant(ClubTournamentParticipant participant);
    
    // Lấy roster theo position
    @Query("SELECT r FROM ClubTournamentRoster r WHERE r.clubTournamentParticipant.id = :participantId AND r.position = :position")
    List<ClubTournamentRoster> findByParticipantIdAndPosition(
            @Param("participantId") String participantId,
            @Param("position") String position
    );
    
    // Xóa member khỏi roster
    void deleteByClubTournamentParticipantAndClubMember(
            ClubTournamentParticipant participant,
            ClubMember clubMember
    );
    
    // Kiểm tra club member có tham gia tournament nào không
    @Query("""
        SELECT r FROM ClubTournamentRoster r
        WHERE r.clubMember.id = :clubMemberId
        AND r.clubTournamentParticipant.tournament.id = :tournamentId
    """)
    List<ClubTournamentRoster> findByClubMemberIdAndTournamentId(
            @Param("clubMemberId") String clubMemberId,
            @Param("tournamentId") String tournamentId
    );

    // Fetch roster voi clubMember + account + userInfo
    @Query("SELECT r FROM ClubTournamentRoster r " +
           "LEFT JOIN FETCH r.clubMember cm " +
           "LEFT JOIN FETCH cm.account a " +
           "LEFT JOIN FETCH a.userInfo " +
           "WHERE r.clubTournamentParticipant.id = :participantId")
    List<ClubTournamentRoster> findByParticipantIdWithDetails(@Param("participantId") String participantId);
}
