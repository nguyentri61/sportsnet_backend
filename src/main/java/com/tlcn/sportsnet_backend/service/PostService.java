package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.post.MediaResponse;
import com.tlcn.sportsnet_backend.dto.post.PostCreateRequest;
import com.tlcn.sportsnet_backend.dto.post.PostResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Post;
import com.tlcn.sportsnet_backend.entity.PostMedia;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ClubRepository clubRepository;
    private final ClubEventRepository clubEventRepository;
    private final AccountRepository accountRepository;
    private final FileStorageService fileStorageService;
    private final PostMediaRepository postMediaRepository;

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));

        System.out.print(account.getUserInfo().getFullName());

        Post post = Post.builder()
                .content(request.getContent())
                .author(account)
                .build();

        // Gắn club
        if (request.getClubId() != null) {
            clubRepository.findBySlug(request.getClubId()).ifPresent(post::setClub);
        }

        // Gắn event
        if (request.getEventId() != null) {
            clubEventRepository.findBySlug(request.getEventId()).ifPresent(post::setEvent);
        }

        post = postRepository.save(post);

        // Lưu file media
        if (request.getFileNames() != null && !request.getFileNames().isEmpty()) {
            for (String filename : request.getFileNames()) {
                PostMedia media = PostMedia.builder()
                        .filename(filename)
                        .type(fileStorageService.detectMediaType(filename))
                        .post(post)
                        .build();
                postMediaRepository.save(media);
            }
        }

        return toResponse(post, account);
    }

    private PostResponse toResponse(Post post, Account account) {

        List<PostMedia> mediaList = postMediaRepository.findByPostId(post.getId());

        List<MediaResponse> mediaResponses = mediaList.stream()
                .map(media -> MediaResponse.builder()
                        .url(fileStorageService.getFileUrl(media.getFilename(), "/posts")) // chỉnh "/post" tùy theo folder bạn lưu
                        .type(media.getType())
                        .build())
                .toList();

        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .authorName(account.getUserInfo().getFullName())
                .authorAvatar(fileStorageService.getFileUrl(account.getUserInfo().getAvatarUrl(), "/avatar"))
                .createdAt(post.getCreatedAt())
                .likeCount(0)
                .commentCount(0)
                .mediaList(mediaResponses)
                .build();
    }
}