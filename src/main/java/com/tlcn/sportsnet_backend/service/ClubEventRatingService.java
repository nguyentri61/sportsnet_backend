package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_event.ClubEventResponse;
import com.tlcn.sportsnet_backend.dto.rating.ClubEventRatingCreateRequest;
import com.tlcn.sportsnet_backend.dto.rating.ClubEventRatingResponse;
import com.tlcn.sportsnet_backend.dto.rating.ClubRatingResponse;
import com.tlcn.sportsnet_backend.dto.rating.ReplyRatingCreateRequest;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import com.tlcn.sportsnet_backend.entity.ClubEventRating;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final ClubMemberRepository clubMemberRepository;
    private final ClubService clubService;

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
        clubEventRating.setClubMember(clubMemberRepository.existsByClubAndAccountAndStatus(clubEvent.getClub(),account, ClubMemberStatusEnum.APPROVED));
        clubEventRating = clubEventRatingRepository.save(clubEventRating);
        clubService.calculateReputation(clubEvent.getClub());
        return toClubEventRatingResponse(clubEventRating);
    }
    public List<ClubEventRatingResponse> getAllByClubEventId(String clubEventId) {
        ClubEvent clubEvent = clubEventRepository.findById(clubEventId)
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
    public ClubRatingResponse getAllByClubId(String clubId) {
        PageRequest pageRequest = PageRequest.of(0, 5); // trang 0, size 5
        List<ClubEventRating> ratings = clubEventRatingRepository.findAllByClubEvent_Club_IdOrderByCreatedAtDesc(clubId, pageRequest);
        Long total = clubEventRatingRepository.countByClubEvent_Club_Id(clubId);
        Double avg = clubEventRatingRepository.getWeightedAverageRatingByClubId(clubId);

        // init count 0 cho từng sao
        long one = 0, two = 0, three = 0, four = 0, five = 0;

        List<Object[]> results = clubEventRatingRepository.getRatingCountsByClubId(clubId);
        for (Object[] row : results) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            switch (rating) {
                case 1 -> one = count;
                case 2 -> two = count;
                case 3 -> three = count;
                case 4 -> four = count;
                case 5 -> five = count;
            }
        }
        List<ClubEventRatingResponse> clubEventRatingResponses =  ratings.stream()
                .map(this::toClubEventRatingResponse)
                .toList();
        return ClubRatingResponse.builder()
                .clubEventRatingResponses(clubEventRatingResponses)
                .averageRating(avg)
                .totalReviews(total)
                .oneStar(one)
                .twoStars(two)
                .threeStars(three)
                .fourStars(four)
                .fiveStars(five)
                .build();
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
    public PagedResponse<ClubEventRatingResponse> getMoreByClubId(String clubId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size); // trang 0, size 5
        Page<ClubEventRating> clubEventRatings = clubEventRatingRepository.findAllByClubEvent_Club_IdOrderByCreatedAtDesc(pageRequest, clubId);

        List<ClubEventRatingResponse> content = clubEventRatings.getContent().stream()
                .map(this::toClubEventRatingResponse)
                .toList();

        return new PagedResponse<>(
                content,
                clubEventRatings.getNumber(),
                clubEventRatings.getSize(),
                clubEventRatings.getTotalElements(),
                clubEventRatings.getTotalPages(),
                clubEventRatings.isLast()
        );
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
                .eventName(clubEventRating.getClubEvent().getTitle())
                .nameSender(clubEventRating.getAccount().getUserInfo().getFullName())
                .avatarUrl(fileStorageService.getFileUrl(clubEventRating.getAccount().getUserInfo().getAvatarUrl(), "/avatar"))
                .createdAt(clubEventRating.getCreatedAt())
                .replyComment(clubEventRating.getReplyComment())
                .replyCreatedAt(clubEventRating.getReplyAt())
                .isClubMember(clubEventRating.isClubMember())
                .build();
    }



}
