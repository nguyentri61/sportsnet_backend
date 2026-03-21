package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_tournament.*;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.*;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubTournamentService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final TournamentRepository tournamentRepository;
    private final ClubTournamentParticipantRepository clubTournamentParticipantRepository;
    private final ClubTournamentRosterRepository clubTournamentRosterRepository;
    private final AccountRepository accountRepository;
    private final PlayerRatingRepository playerRatingRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    // =========================================================
    // 1. ĐĂNG KÝ CLB THAM GIA TOURNAMENT
    // =========================================================

    @Transactional
    public ClubTournamentParticipantResponse registerClub(String tournamentId, ClubTournamentRegistrationRequest request) {
        // Validate request null
        if (request == null) {
            throw new InvalidDataException("Request khong duoc de trong");
        }
        if (request.getClubId() == null || request.getClubId().isBlank()) {
            throw new InvalidDataException("clubId khong duoc de trong");
        }

        List<String> rosterIds = request.getRosterAccountIds();
        if (rosterIds == null || rosterIds.isEmpty()) {
            throw new InvalidDataException("rosterAccountIds khong duoc de trong");
        }

        Account owner = getCurrentAccount();

        Club club = clubRepository.findByIdWithOwner(request.getClubId())
                .orElseThrow(() -> new InvalidDataException("Khong tim thay CLB"));

        // Kiểm tra quyền: chỉ OWNER mới được đăng ký
        ClubMember ownerMember = clubMemberRepository.findByClubAndAccountWithAccount(club, owner);
        if (ownerMember == null || ownerMember.getRole() != ClubMemberRoleEnum.OWNER) {
            throw new InvalidDataException("Chỉ chủ CLB mới có thể đăng ký tham gia tournament");
        }

        // Kiểm tra CLB đang hoạt động
        if (club.getStatus() != ClubStatusEnum.ACTIVE) {
            throw new InvalidDataException("CLB phải ở trạng thái ACTIVE mới có thể đăng ký tournament");
        }

        Tournament tournament = tournamentRepository.findByIdForClubTournament(tournamentId)
                .orElseThrow(() -> new InvalidDataException("Khong tim thay giai dau"));

        // Kiểm tra tournament phải là loại CLUB
        if (tournament.getParticipationType() != TournamentParticipationTypeEnum.CLUB) {
            throw new InvalidDataException("Tournament này không dành cho CLB đăng ký");
        }

        // Kiểm tra thời gian đăng ký
        validateRegistrationDate(tournament);

        // Kiểm tra CLB đã đăng ký tournament này chưa
        if (clubTournamentParticipantRepository.existsByClubAndTournament(club, tournament)) {
            throw new InvalidDataException("CLB đã đăng ký giải đấu này rồi");
        }

        // Kiểm tra số lượng CLB đã đăng ký
        int currentCount = clubTournamentParticipantRepository.countByTournament(tournament);
        if (tournament.getMaxClubs() != null && currentCount >= tournament.getMaxClubs()) {
            throw new InvalidDataException("Giải đấu đã đủ số lượng CLB tham gia");
        }

        // Validate danh sách roster
        validateRosterSize(tournament, rosterIds);
        List<ClubMember> rosterMembers = validateRosterMembersByAccountIds(club, tournament, rosterIds);

        // Tạo ClubTournamentParticipant
        ClubTournamentParticipant participant = ClubTournamentParticipant.builder()
                .club(club)
                .tournament(tournament)
                .status(ClubTournamentParticipantStatusEnum.PENDING)
                .build();
        clubTournamentParticipantRepository.save(participant);

        // Tạo roster entries
        List<ClubTournamentRoster> rosterEntries = new ArrayList<>();
        for (ClubMember member : rosterMembers) {
            rosterEntries.add(ClubTournamentRoster.builder()
                    .clubTournamentParticipant(participant)
                    .clubMember(member)
                    .canModify(true)
                    .build());
        }
        clubTournamentRosterRepository.saveAll(rosterEntries);
        participant.setRoster(rosterEntries);

        // Build response - fetch data eagerly to avoid lazy loading issues
        ClubTournamentParticipantResponse response = buildFullResponse(participant);

        // TODO: Gửi thông báo cho các thành viên trong roster sau

        return response;
    }

    // =========================================================
    // 2. CẬP NHẬT ROSTER (trước deadline)
    // =========================================================

    @Transactional
    public ClubTournamentParticipantResponse updateRoster(String participantId, UpdateRosterRequest request) {
        if (request == null) {
            throw new InvalidDataException("Request is required");
        }

        // Support both rosterAccountIds and rosterMemberIds
        List<String> rosterIds = request.getRosterAccountIds();
        if (rosterIds == null || rosterIds.isEmpty()) {
            rosterIds = request.getRosterMemberIds();
        }
        if (rosterIds == null || rosterIds.isEmpty()) {
            throw new InvalidDataException("rosterAccountIds is required");
        }

        Account owner = getCurrentAccount();

        ClubTournamentParticipant participant = clubTournamentParticipantRepository.findByIdWithDetails(participantId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đăng ký CLB"));

        Club club = participant.getClub();

        // Kiểm tra quyền
        ClubMember ownerMember = clubMemberRepository.findByClubAndAccountWithAccount(club, owner);
        if (ownerMember == null || ownerMember.getRole() != ClubMemberRoleEnum.OWNER) {
            throw new InvalidDataException("Chỉ chủ CLB mới có thể cập nhật roster");
        }

        // Chỉ cho phép cập nhật khi status là PENDING hoặc DRAFT, PAYMENT_REQUIRED
        if (participant.getStatus() == ClubTournamentParticipantStatusEnum.APPROVED
                || participant.getStatus() == ClubTournamentParticipantStatusEnum.CANCELLED
                || participant.getStatus() == ClubTournamentParticipantStatusEnum.ELIMINATED
                || participant.getStatus() == ClubTournamentParticipantStatusEnum.REJECTED) {
            throw new InvalidDataException("Không thể cập nhật roster ở trạng thái " + participant.getStatus());
        }

        Tournament tournament = participant.getTournament();

        // Kiểm tra còn trong thời gian đăng ký
        validateRegistrationDate(tournament);

        // Validate roster mới
        validateRosterSize(tournament, rosterIds);
        List<ClubMember> rosterMembers = validateRosterMembersByAccountIds(club, tournament, rosterIds);

        // Xóa roster cũ, tạo roster mới
        List<ClubTournamentRoster> oldRoster = clubTournamentRosterRepository.findByClubTournamentParticipant(participant);
        clubTournamentRosterRepository.deleteAll(oldRoster);

        List<ClubTournamentRoster> newRoster = new ArrayList<>();
        for (ClubMember member : rosterMembers) {
            newRoster.add(ClubTournamentRoster.builder()
                    .clubTournamentParticipant(participant)
                    .clubMember(member)
                    .canModify(true)
                    .build());
        }
        clubTournamentRosterRepository.saveAll(newRoster);
        participant.setRoster(newRoster);

        return toResponse(participant, false);
    }

    // =========================================================
    // 3. HỦY ĐĂNG KÝ
    // =========================================================

    @Transactional
    public void cancelRegistration(String participantId) {
        Account owner = getCurrentAccount();

        ClubTournamentParticipant participant = clubTournamentParticipantRepository.findByIdWithDetails(participantId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đăng ký CLB"));

        ClubMember ownerMember = clubMemberRepository.findByClubAndAccountWithAccount(participant.getClub(), owner);
        if (ownerMember == null || ownerMember.getRole() != ClubMemberRoleEnum.OWNER) {
            throw new InvalidDataException("Chỉ chủ CLB mới có thể hủy đăng ký");
        }

        if (participant.getStatus() == ClubTournamentParticipantStatusEnum.ELIMINATED
                || participant.getStatus() == ClubTournamentParticipantStatusEnum.CANCELLED) {
            throw new InvalidDataException("Đăng ký đã ở trạng thái không thể hủy");
        }

        participant.setStatus(ClubTournamentParticipantStatusEnum.CANCELLED);
        clubTournamentParticipantRepository.save(participant);
    }

    // =========================================================
    // 4. ADMIN: DUYỆT / TỪ CHỐI CLB
    // =========================================================

    @Transactional
    public void approveClubParticipant(String participantId) {
        // 1. Validate admin
        validateAdmin();

        // 2. Load participant với eager fetch
        ClubTournamentParticipant participant = clubTournamentParticipantRepository.findByIdWithDetails(participantId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đăng ký CLB"));

        // 3. Kiểm tra status hợp lệ (chỉ PENDING mới được duyệt - thanh toán sẽ implement sau)
        if (participant.getStatus() != ClubTournamentParticipantStatusEnum.PENDING) {
            throw new InvalidDataException("Chỉ có thể duyệt đăng ký ở trạng thái PENDING");
        }

        // 4. Kiểm tra club còn ACTIVE
        Club club = participant.getClub();
        if (club.getStatus() != ClubStatusEnum.ACTIVE) {
            throw new InvalidDataException("CLB không còn ở trạng thái ACTIVE");
        }

        // 5. Kiểm tra tournament chưa start
        Tournament tournament = participant.getTournament();
        if (tournament.getStartDate() != null && LocalDateTime.now().isAfter(tournament.getStartDate())) {
            throw new InvalidDataException("Giải đấu đã bắt đầu, không thể duyệt thêm CLB");
        }

        // 6. Kiểm tra capacity (số CLB đã approve < maxClubs)
        int approvedCount = (int) clubTournamentParticipantRepository.findByTournamentIdAndStatus(
                tournament.getId(), ClubTournamentParticipantStatusEnum.APPROVED).size();
        if (tournament.getMaxClubs() != null && approvedCount >= tournament.getMaxClubs()) {
            throw new InvalidDataException("Giải đấu đã đủ số lượng CLB được duyệt");
        }

        // 7. Approve
        participant.setStatus(ClubTournamentParticipantStatusEnum.APPROVED);
        clubTournamentParticipantRepository.save(participant);

        // 8. Gửi notification cho owner
        notificationService.sendToAccount(
                club.getOwner().getEmail(),
                "CLB được duyệt tham gia tournament",
                String.format("CLB %s đã được duyệt tham gia giải đấu %s",
                        club.getName(), tournament.getName()),
                "/tournament/" + tournament.getSlug()
        );

        // 9. Gửi notification cho các thành viên trong roster
        List<ClubTournamentRoster> roster = participant.getRoster();
        if (roster != null) {
            for (ClubTournamentRoster entry : roster) {
                notificationService.sendToAccount(
                        entry.getClubMember().getAccount().getEmail(),
                        "Thông báo tham gia tournament",
                        String.format("CLB %s của bạn đã được duyệt tham gia giải đấu %s",
                                club.getName(), tournament.getName()),
                        "/tournament/" + tournament.getSlug()
                );
            }
        }
    }

    @Transactional
    public void rejectClubParticipant(String participantId) {
        validateAdmin();

        ClubTournamentParticipant participant = clubTournamentParticipantRepository.findByIdWithDetails(participantId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đăng ký CLB"));

        participant.setStatus(ClubTournamentParticipantStatusEnum.REJECTED);
        clubTournamentParticipantRepository.save(participant);

        notificationService.sendToAccount(
                participant.getClub().getOwner().getEmail(),
                "CLB bị từ chối tham gia tournament",
                String.format("CLB %s đã bị từ chối tham gia giải đấu %s",
                        participant.getClub().getName(),
                        participant.getTournament().getName()),
                "/tournament/" + participant.getTournament().getSlug()
        );
    }

    // =========================================================
    // 5. QUERY: DANH SÁCH CLB TRONG TOURNAMENT
    // =========================================================

    public PagedResponse<ClubTournamentParticipantResponse> getAllClubParticipants(
            String tournamentId,
            List<ClubTournamentParticipantStatusEnum> statuses,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("registeredAt").descending());

        Page<ClubTournamentParticipant> participantPage;
        if (statuses == null || statuses.isEmpty()) {
            participantPage = clubTournamentParticipantRepository.findByTournamentId(tournamentId, pageable);
        } else {
            participantPage = clubTournamentParticipantRepository.findByTournamentIdAndStatusIn(tournamentId, statuses, pageable);
        }

        List<ClubTournamentParticipantResponse> content = participantPage.getContent()
                .stream()
                .map(p -> toResponse(p, false))
                .toList();

        return new PagedResponse<>(content, participantPage.getNumber(), participantPage.getSize(),
                participantPage.getTotalElements(), participantPage.getTotalPages(), participantPage.isLast());
    }

    // =========================================================
    // 6. QUERY: TOURNAMENTS CỦA CLB (My Club - Tab Giải đấu)
    // =========================================================

    public List<ClubTournamentParticipantResponse> getMyClubTournaments(String clubId) {
        Account account = getCurrentAccount();

        // Verify club exists
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new InvalidDataException("Khong tim thay CLB"));

        // Verify caller is club owner
        ClubMember ownerMember = clubMemberRepository.findByClubAndAccountWithAccount(club, account);
        if (ownerMember == null || ownerMember.getRole() != ClubMemberRoleEnum.OWNER) {
            throw new InvalidDataException("Chi chu CLB moi co the xem danh sach");
        }

        // Get all tournaments for this club
        List<ClubTournamentParticipant> participants = clubTournamentParticipantRepository
                .findByClubIdWithDetails(clubId);

        return participants.stream()
                .map(p -> toResponse(p, false))  // false = basic info, no full roster
                .toList();
    }

    // QUERY: Chi tiết đăng ký của một CLB (dùng cho admin hoặc club owner)
    public ClubTournamentParticipantResponse getClubParticipantDetail(String participantId) {
        ClubTournamentParticipant participant = clubTournamentParticipantRepository.findByIdWithDetails(participantId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đăng ký CLB"));
        return toResponse(participant, true);
    }

    // QUERY: CLB của người dùng hiện tại đã đăng ký tournament này chưa
    public ClubTournamentParticipantResponse getMyClubParticipation(String tournamentId, String clubId) {
        Account account = getCurrentAccount();
        Club club = clubRepository.findByIdWithOwner(clubId)
                .orElseThrow(() -> new InvalidDataException("Khong tim thay CLB"));

        // Chỉ owner mới được xem
        ClubMember ownerMember = clubMemberRepository.findByClubAndAccountWithAccount(club, account);
        if (ownerMember == null || ownerMember.getRole() != ClubMemberRoleEnum.OWNER) {
            throw new InvalidDataException("Chỉ chủ CLB mới có thể xem thông tin đăng ký");
        }

        Tournament tournament = tournamentRepository.findByIdForClubTournament(tournamentId)
                .orElseThrow(() -> new InvalidDataException("Khong tim thay giai dau"));

        return clubTournamentParticipantRepository.findByClubAndTournament(club, tournament)
                .map(p -> toResponse(p, true))
                .orElse(null);
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private void validateRegistrationDate(Tournament tournament) {
        LocalDateTime now = LocalDateTime.now();
        // Use registrationEndDate from tournament (common for both individual and club)
        LocalDateTime deadline = tournament.getRegistrationEndDate();

        if (tournament.getRegistrationStartDate() != null && now.isBefore(tournament.getRegistrationStartDate())) {
            throw new InvalidDataException("Giải đấu chưa mở đăng ký");
        }
        if (deadline != null && now.isAfter(deadline)) {
            throw new InvalidDataException("Giải đấu đã hết thời gian đăng ký");
        }
    }

    private void validateRosterSize(Tournament tournament, List<String> rosterIds) {
        if (rosterIds == null || rosterIds.isEmpty()) {
            throw new InvalidDataException("Danh sách roster không được để trống");
        }
        // Check for duplicates
        if (rosterIds.size() != rosterIds.stream().distinct().count()) {
            throw new InvalidDataException("Danh sách roster không được chứa trùng lặp");
        }
        if (tournament.getMinClubRosterSize() != null && rosterIds.size() < tournament.getMinClubRosterSize()) {
            throw new InvalidDataException("Roster cần ít nhất " + tournament.getMinClubRosterSize() + " thành viên");
        }
        if (tournament.getMaxClubRosterSize() != null && rosterIds.size() > tournament.getMaxClubRosterSize()) {
            throw new InvalidDataException("Roster không được vượt quá " + tournament.getMaxClubRosterSize() + " thành viên");
        }
    }

    /**
     * Validate roster members by account IDs.
     * rosterIds now accepts account IDs (not clubMemberId)
     */
    private List<ClubMember> validateRosterMembersByAccountIds(Club club, Tournament tournament, List<String> accountIds) {
        // Fetch all club members with account info once to avoid N+1
        List<ClubMember> allClubMembers = clubMemberRepository.findByClubIdWithAccount(club.getId());

        List<ClubMember> members = new ArrayList<>();
        for (String accountId : accountIds) {
            // Find ClubMember by account ID from pre-fetched list
            ClubMember member = allClubMembers.stream()
                    .filter(m -> m.getAccount().getId().equals(accountId))
                    .findFirst()
                    .orElse(null);

            if (member == null) {
                throw new InvalidDataException("Khong tim thay thanh vien voi account ID: " + accountId + " trong CLB nay");
            }

            // Phai co trang thai APPROVED
            if (member.getStatus() != ClubMemberStatusEnum.APPROVED) {
                throw new InvalidDataException("Thanh vien " + member.getAccount().getUserInfo().getFullName() + " chua duoc duyet vao CLB");
            }

            members.add(member);
        }
        return members;
    }

    private double parseSkillLevel(String skillLevel) {
        try {
            return Double.parseDouble(skillLevel);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void validateAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
        boolean isAdmin = account.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new InvalidDataException("Chỉ admin mới có quyền thực hiện thao tác này");
        }
    }

    private Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
    }

    // Build response với full roster (dùng trong registerClub)
    private ClubTournamentParticipantResponse buildResponse(ClubTournamentParticipant participant) {
        // Roster đã được set từ trước khi gọi method này
        List<ClubTournamentRoster> rosterList = participant.getRoster();
        List<ClubRosterMemberResponse> rosterResponses = buildRosterResponses(rosterList);
        return buildBaseResponse(participant, rosterResponses);
    }

    // Response đầy đủ (bao gồm full roster) - dùng cho detail
    private ClubTournamentParticipantResponse buildFullResponse(ClubTournamentParticipant participant) {
        List<ClubTournamentRoster> rosterList = participant.getRoster();
        if (rosterList == null || rosterList.isEmpty()) {
            rosterList = clubTournamentRosterRepository.findByClubTournamentParticipant(participant);
        }
        List<ClubRosterMemberResponse> rosterResponses = buildRosterResponses(rosterList);
        return buildBaseResponse(participant, rosterResponses);
    }

    // Response cơ bản (không bao gồm roster chi tiết) - dùng cho danh sách phân trang
    private ClubTournamentParticipantResponse buildBasicResponse(ClubTournamentParticipant participant) {
        Club club = participant.getClub();
        Tournament tournament = participant.getTournament();
        int rosterSize = participant.getRoster() != null ? participant.getRoster().size() : 0;
        return ClubTournamentParticipantResponse.builder()
                .id(participant.getId())
                .clubId(club.getId())
                .clubName(club.getName())
                .clubLogoUrl(fileStorageService.getFileUrl(club.getLogoUrl(), "/club/logo"))
                .clubSlug(club.getSlug())
                .clubLocation(club.getLocation())
                .ownerName(club.getOwner().getUserInfo().getFullName())
                .ownerEmail(club.getOwner().getEmail())
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .tournamentSlug(tournament.getSlug())
                .status(participant.getStatus())
                .registeredAt(participant.getRegisteredAt())
                .rosterSize(rosterSize)
                .build();
    }

    // Shared base builder - tránh duplicate code
    private ClubTournamentParticipantResponse buildBaseResponse(ClubTournamentParticipant participant,
                                                                 List<ClubRosterMemberResponse> rosterResponses) {
        Club club = participant.getClub();
        Tournament tournament = participant.getTournament();
        return ClubTournamentParticipantResponse.builder()
                .id(participant.getId())
                .clubId(club.getId())
                .clubName(club.getName())
                .clubLogoUrl(fileStorageService.getFileUrl(club.getLogoUrl(), "/club/logo"))
                .clubSlug(club.getSlug())
                .clubLocation(club.getLocation())
                .ownerName(club.getOwner().getUserInfo().getFullName())
                .ownerEmail(club.getOwner().getEmail())
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .tournamentSlug(tournament.getSlug())
                .status(participant.getStatus())
                .registeredAt(participant.getRegisteredAt())
                .roster(rosterResponses)
                .rosterSize(rosterResponses != null ? rosterResponses.size() : 0)
                .build();
    }

    // Helper: Build roster member responses
    private List<ClubRosterMemberResponse> buildRosterResponses(List<ClubTournamentRoster> rosterList) {
        List<ClubRosterMemberResponse> rosterResponses = new ArrayList<>();
        if (rosterList != null) {
            for (ClubTournamentRoster entry : rosterList) {
                ClubMember member = entry.getClubMember();
                Account account = member.getAccount();
                String skillLevel = playerRatingRepository.findByAccount(account)
                        .map(PlayerRating::getSkillLevel)
                        .orElse("Chua co");

                rosterResponses.add(ClubRosterMemberResponse.builder()
                        .rosterEntryId(entry.getId())
                        .clubMemberId(member.getId())
                        .accountId(account.getId())
                        .fullName(account.getUserInfo().getFullName())
                        .email(account.getEmail())
                        .avatarUrl(fileStorageService.getFileUrl(account.getUserInfo().getAvatarUrl(), "/avatar"))
                        .slug(account.getUserInfo().getSlug())
                        .skillLevel(skillLevel)
                        .role(member.getRole().name())
                        .position(entry.getPosition())
                        .canModify(entry.getCanModify())
                        .build());
            }
        }
        return rosterResponses;
    }

    private ClubTournamentParticipantResponse toResponse(ClubTournamentParticipant participant, boolean includeFullRoster) {
        if (includeFullRoster) {
            return buildFullResponse(participant);
        } else {
            return buildBasicResponse(participant);
        }
    }
}
