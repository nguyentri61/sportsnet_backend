package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_event.ClubEventResponse;
import com.tlcn.sportsnet_backend.dto.rating.ClubEventRatingCreateRequest;
import com.tlcn.sportsnet_backend.dto.rating.ClubEventRatingResponse;
import com.tlcn.sportsnet_backend.dto.rating.ReplyRatingCreateRequest;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.entity.ClubEventRating;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventParticipantRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventRatingRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClubEventRatingService {
    private final ClubEventRatingRepository clubEventRatingRepository;
    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final AccountRepository accountRepository;
    private final ClubEventRepository clubEventRepository;
    private final FileStorageService fileStorageService;
    public ClubEventRatingResponse createClubEvent(ClubEventRatingCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
        ClubEvent clubEvent = clubEventRepository.findById(request.getEventClubId()).orElseThrow(() -> new InvalidDataException("Không tìm thấy hoạt động"));
        if(clubEvent.getStatus()!= EventStatusEnum.FINISHED) {
            throw new InvalidDataException("Không thể đánh giá sự kiện chưa hoàn thành");
        }
        if (clubEvent.getClub().getOwner()==account) {
            throw new InvalidDataException("Chủ CLB không thể tự đánh giá");
        }
        if(!clubEventParticipantRepository.existsByClubEventAndParticipant(clubEvent, account)) {
            throw new InvalidDataException("Không thể đánh giá sự kiện");
        }
        Optional<ClubEventRating> clubEventRatingOptional = clubEventRatingRepository.findByClubEventIdAndAccountId(clubEvent.getId(), account.getId());
        ClubEventRating clubEventRating = clubEventRatingOptional.orElseGet(ClubEventRating::new);
        clubEventRating.setClubEvent(clubEvent);
        clubEventRating.setAccount(account);
        clubEventRating.setRating(request.getRating());
        clubEventRating.setComment(request.getComment());
        clubEventRating = clubEventRatingRepository.save(clubEventRating);
        return toClubEventRatingResponse(clubEventRating);
    }
    public List<ClubEventRatingResponse> getAllByClubId(String clubId) {
        ClubEvent clubEvent = clubEventRepository.findById(clubId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy hoạt động"));

        List<ClubEventRating> clubEventRatings =
                clubEventRatingRepository.findByClubEventIdOrderByCreatedAtDesc(clubEvent.getId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElse(null);

        String currentAccountId = account != null ? account.getId() : null;

        // Sort lại: nếu là rating của currentAccount thì cho lên đầu
        List<ClubEventRatingResponse> sorted = clubEventRatings.stream()
                .sorted((r1, r2) -> {
                    if (currentAccountId != null) {
                        if (r1.getAccount().getId().equals(currentAccountId)) return -1;
                        if (r2.getAccount().getId().equals(currentAccountId)) return 1;
                    }
                    // Các rating còn lại thì sort theo createdAt desc
                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                })
                .map(this::toClubEventRatingResponse)
                .toList();

        return sorted;
    }
    public ClubEventRatingResponse createReplyRatingClubEvent(ReplyRatingCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
        ClubEventRating clubEventRating = clubEventRatingRepository.findById(request.getRatingId()).orElseThrow(() -> new InvalidDataException("Không tìm thấy đánh giá"));
        if(account!=clubEventRating.getClubEvent().getClub().getOwner()) {
            throw new InvalidDataException("Không có quyền phản hồi");
        }
        clubEventRating.setReplyComment(request.getReplyComment());
        clubEventRating.setReplyBy(account);
        clubEventRating.setReplyAt(Instant.now());
        clubEventRating = clubEventRatingRepository.save(clubEventRating);
        return toClubEventRatingResponse(clubEventRating);
    }
    public ClubEventRatingResponse getOwnClubEventRatingByClubId(String clubId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
        ClubEvent clubEvent = clubEventRepository.findById(clubId).orElseThrow(() -> new InvalidDataException("Không tìm thấy hoạt động"));
        Optional<ClubEventRating> clubEventRating = clubEventRatingRepository.findByClubEventIdAndAccountId(clubEvent.getId(), account.getId());
        return clubEventRating.map(this::toClubEventRatingResponse).orElse(null);
    }
    public ClubEventRatingResponse toClubEventRatingResponse(ClubEventRating clubEventRating) {
        return ClubEventRatingResponse.builder()
                .id(clubEventRating.getId())
                .comment(clubEventRating.getComment())
                .rating(clubEventRating.getRating())
                .eventClubId(clubEventRating.getClubEvent().getId())
                .nameSender(clubEventRating.getAccount().getUserInfo().getFullName())
                .avatarUrl(fileStorageService.getFileUrl(clubEventRating.getAccount().getUserInfo().getAvatarUrl(), "/avatar"))
                .createdAt(clubEventRating.getCreatedAt())
                .replyComment(clubEventRating.getReplyComment())
                .replyCreatedAt(clubEventRating.getReplyAt())
                .build();
    }



}
