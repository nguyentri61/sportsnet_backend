package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.post.MediaResponse;
import com.tlcn.sportsnet_backend.dto.post.PostCreateRequest;
import com.tlcn.sportsnet_backend.dto.post.PostResponse;
import com.tlcn.sportsnet_backend.dto.post.PostUpdateRequest;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

        Post post = Post.builder()
                .content(request.getContent())
                .author(account)
                .build();

        // Gắn event
        if (request.getEventId() != null) {
            clubEventRepository.findById(request.getEventId()).ifPresent(post::setEvent);
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account currentAccount = accountRepository.findByEmail(authentication.getName()).orElse(null);

        List<PostMedia> mediaList = postMediaRepository.findByPostId(post.getId());

        List<MediaResponse> mediaResponses = mediaList.stream()
                .map(media -> MediaResponse.builder()
                        .fileName(media.getFilename())
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
                .userId(account.getId())
                .currentUserId(currentAccount != null ? currentAccount.getId() : null)
                .build();
    }

    public List<PostResponse> getAllPostsByEvent(String eventId) {
        List<Post> postList = postRepository.findByEventIdOrderByCreatedAtDesc(eventId);

        return postList.stream()
                .map(post -> toResponse(post, post.getAuthor()))
                .toList();

    }

    public PostResponse updatePost(PostUpdateRequest request) {
        Post post = postRepository.findById(request.getId()).orElseThrow(() -> new InvalidDataException("Post not found"));

        post.setContent(request.getContent());

        // Lấy danh sách media hiện có trong DB
        List<PostMedia> currentMedia = postMediaRepository.findByPostId(post.getId());

        // 1. Xóa media không còn trong keepFileNames
        if (request.getKeepFileNames() != null) {
            for (PostMedia media : currentMedia) {
                if (!request.getKeepFileNames().contains(media.getFilename())) {
                    fileStorageService.deleteFile(media.getFilename(), "/posts");
                    postMediaRepository.delete(media);
                }
            }
        }

        // 2. Thêm file mới (nếu có)
        if (request.getNewFileNames() != null && !request.getNewFileNames().isEmpty()) {
            for (String filename : request.getNewFileNames()) {
                PostMedia media = PostMedia.builder()
                        .filename(filename)
                        .type(fileStorageService.detectMediaType(filename))
                        .post(post)
                        .build();
                postMediaRepository.save(media);
            }
        }
        post = postRepository.save(post);
        return toResponse(post, post.getAuthor());
    }

    public void deletePost(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));

        Post post = postRepository.findById(id).orElseThrow(() -> new InvalidDataException("Post not found"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
        boolean isClubOwner = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_CLUB_OWNER".equals(grantedAuthority.getAuthority()));
        boolean isAuthor = post.getAuthor() != null && post.getAuthor().getId().equals(account.getId());

        if (!(isAdmin || isClubOwner || isAuthor)) {
            throw new SecurityException("Bạn không có quyền xóa bài viết này");
        }

        List<PostMedia> postMedia = postMediaRepository.findByPostId(post.getId());

        for (PostMedia media : postMedia) {
            fileStorageService.deleteFile(media.getFilename(), "/posts");
        }
        postMediaRepository.deleteAll(postMedia);

        postRepository.delete(post);
    }
}