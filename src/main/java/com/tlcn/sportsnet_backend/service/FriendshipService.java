package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.account.AccountFriend;
import com.tlcn.sportsnet_backend.dto.friend.FriendRequest;
import com.tlcn.sportsnet_backend.dto.friend.FriendResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Friendship;
import com.tlcn.sportsnet_backend.entity.PlayerRating;
import com.tlcn.sportsnet_backend.enums.FriendStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.FriendshipRepository;
import com.tlcn.sportsnet_backend.repository.PlayerRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final AccountRepository accountRepository;
    private final FileStorageService fileStorageService;
    private final PlayerRatingRepository playerRatingRepository;
    private final NotificationService notificationService;

    public FriendResponse getRelationship(String accountId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account currentAccount = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));

        Account otherAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new InvalidDataException("Other user not found"));

        if (currentAccount.getId().equals(otherAccount.getId())) {
            throw new InvalidDataException("Cannot check relationship with yourself");
        }

        Optional<Friendship> relation = friendshipRepository.findBetween(currentAccount, otherAccount);

        if (relation.isEmpty()) {
            // Không tạo mới nữa, chỉ trả về NONE
            return FriendResponse.builder()
                    .status(FriendStatusEnum.NONE)
                    .requester(FriendResponse.UserSummary.builder()
                            .id(currentAccount.getId())
                            .email(currentAccount.getEmail())
                            .fullName(currentAccount.getUserInfo() != null
                                    ? currentAccount.getUserInfo().getFullName()
                                    : null)
                            .build())
                    .receiver(FriendResponse.UserSummary.builder()
                            .id(otherAccount.getId())
                            .email(otherAccount.getEmail())
                            .fullName(otherAccount.getUserInfo() != null
                                    ? otherAccount.getUserInfo().getFullName()
                                    : null)
                            .build())
                    .build();
        }

        return toResponse(relation.get());
    }

    @Transactional
    public FriendResponse sendFriendRequest(FriendRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account requester = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        Account receiver = accountRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new InvalidDataException("Receiver not found"));

        if (requester.getId().equals(receiver.getId())) {
            throw new InvalidDataException("Cannot send friend request to yourself");
        }

        Optional<Friendship> existing = friendshipRepository.findBetween(requester, receiver);

        Friendship friendship;
        if (existing.isPresent()) {
            friendship = existing.get();
            if (friendship.getStatus() == FriendStatusEnum.NONE || friendship.getStatus() == FriendStatusEnum.REJECTED) {
                friendship.setRequester(requester);
                friendship.setReceiver(receiver);
                friendship.setStatus(FriendStatusEnum.PENDING);
                friendship = friendshipRepository.save(friendship);
            } else {
                throw new InvalidDataException("Friendship already exists with status: " + friendship.getStatus());
            }
        } else {
            friendship = Friendship.builder()
                    .requester(requester)
                    .receiver(receiver)
                    .status(FriendStatusEnum.PENDING)
                    .build();
            friendship = friendshipRepository.save(friendship);
        }
        notificationService.sendToAccount(receiver,"Lời mời kết bạn!" ,requester.getUserInfo().getFullName() + " đã lời mời kết bạn đến bạn ","/profile/" + requester.getUserInfo().getSlug());
        return toResponse(friendship);
    }

    private FriendResponse toResponse(Friendship friendship) {
        return FriendResponse.builder()
                .id(friendship.getId())
                .status(friendship.getStatus())
                .createdAt(friendship.getCreatedAt())
                .requester(FriendResponse.UserSummary.builder()
                        .id(friendship.getRequester().getId())
                        .email(friendship.getRequester().getEmail())
                        .fullName(friendship.getRequester().getUserInfo() != null
                                ? friendship.getRequester().getUserInfo().getFullName()
                                : null)
                        .build())
                .receiver(FriendResponse.UserSummary.builder()
                        .id(friendship.getReceiver().getId())
                        .email(friendship.getReceiver().getEmail())
                        .fullName(friendship.getReceiver().getUserInfo() != null
                                ? friendship.getReceiver().getUserInfo().getFullName()
                                : null)
                        .build())
                .build();
    }

    public FriendResponse acceptFriendRequest(String requesterId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account receiver = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        Account requester = accountRepository.findById(requesterId)
                .orElseThrow(() -> new InvalidDataException("Requester not found"));

        Friendship friendship = friendshipRepository.findBetween(requester, receiver)
                .orElseThrow(() -> new InvalidDataException("Friend request not found"));

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new RuntimeException("You are not allowed to accept this request");
        }
        if (friendship.getStatus() != FriendStatusEnum.PENDING) {
            throw new InvalidDataException("Invalid request state: " + friendship.getStatus());
        }

        friendship.setStatus(FriendStatusEnum.ACCEPTED);
        friendship = friendshipRepository.save(friendship);

        notificationService.sendToAccount(requester,"Chấp nhận lời mời kết bạn!" ,receiver.getUserInfo().getFullName() + " đã chấp nhận lời mời kết bạn của bạn ","/profile/" + receiver.getUserInfo().getSlug());
        return toResponse(friendship);
    }

    public FriendResponse rejectFriendRequest(String requesterId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account receiver = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        Account requester = accountRepository.findById(requesterId)
                .orElseThrow(() -> new InvalidDataException("Requester not found"));

        Friendship friendship = friendshipRepository.findBetween(requester, receiver)
                .orElseThrow(() -> new InvalidDataException("Friend request not found"));

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new RuntimeException("You are not allowed to reject this request");
        }
        if (friendship.getStatus() != FriendStatusEnum.PENDING) {
            throw new InvalidDataException("Invalid request state: " + friendship.getStatus());
        }

        friendship.setStatus(FriendStatusEnum.REJECTED);
        friendship = friendshipRepository.save(friendship);

        notificationService.sendToAccount(requester,"Từ chối lời mời kết bạn!" ,receiver.getUserInfo().getFullName() + " đã từ chối lời mời kết bạn của bạn ","/profile/" + receiver.getUserInfo().getSlug());
        return toResponse(friendship);
    }

    public List<AccountFriend> getAllFriends(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<Friendship> friendships = friendshipRepository.findAllFriends(account);

        // Chuyển về list Account bạn bè
        List<Account> listFriendAccount = friendships.stream()
                .map(f -> f.getRequester().equals(account) ? f.getReceiver() : f.getRequester())
                .toList();

        return listFriendAccount.stream()
                .map(x -> AccountFriend.builder()
                        .id(x.getId())
                        .avatarUrl(fileStorageService.getFileUrl(x.getUserInfo().getAvatarUrl(), "/avatar"))
                        .fullName(x.getUserInfo().getFullName())
                        .skillLevel(playerRatingRepository.findByAccount(x)
                                .map(PlayerRating::getSkillLevel)
                                .orElse("Chưa có"))
                        .slug(x.getUserInfo().getSlug())
                        .build())
                .toList();
    }

    public List<AccountFriend> getAllRequesters() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));

        List<Friendship> friendships = friendshipRepository.findAllFriendsByReceiverAndStatus(
                account,
                FriendStatusEnum.PENDING,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Account> listAccountRequest = friendships.stream()
                .map(Friendship::getRequester)
                .toList();

        return listAccountRequest.stream()
                .map(x -> AccountFriend.builder()
                        .id(x.getId())
                        .avatarUrl(fileStorageService.getFileUrl(x.getUserInfo().getAvatarUrl(), "/avatar"))
                        .fullName(x.getUserInfo().getFullName())
                        .skillLevel(playerRatingRepository.findByAccount(x)
                                .map(PlayerRating::getSkillLevel)
                                .orElse("Chưa có"))
                        .slug(x.getUserInfo().getSlug())
                        .build())
                .toList();


    }
}
