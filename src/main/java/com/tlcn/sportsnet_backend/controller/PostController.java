package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.ApiResponse;
import com.tlcn.sportsnet_backend.dto.post.PostCreateRequest;
import com.tlcn.sportsnet_backend.dto.post.PostResponse;
import com.tlcn.sportsnet_backend.service.FileStorageService;
import com.tlcn.sportsnet_backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostCreateRequest request) {
        PostResponse postResponse = postService.createPost(request);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body("No files uploaded");
        }

        List<String> uploadedFiles = files.stream()
                .filter(f -> !f.isEmpty())
                .map(f -> fileStorageService.storeFile(f, "/posts")) // lưu file vào thư mục posts
                .toList();

        if (uploadedFiles.size() == 1) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(Map.of("fileName", uploadedFiles.getFirst())));
        } else {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(Map.of("fileNames", uploadedFiles)));
        }
    }
}
