package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.ApiResponse;
import com.tlcn.sportsnet_backend.dto.post.PostCreateRequest;
import com.tlcn.sportsnet_backend.dto.post.PostResponse;
import com.tlcn.sportsnet_backend.dto.post.PostUpdateRequest;
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

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getAllPostsByEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(postService.getAllPostsByEvent(eventId));
    }


    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostCreateRequest request) {
        PostResponse postResponse = postService.createPost(request);
        return ResponseEntity.ok(postResponse);
    }

    @PutMapping
    public ResponseEntity<PostResponse> updatePost(@RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(postService.updatePost(request));
    }

    @DeleteMapping
    public ResponseEntity<?> deletePost(@PathVariable String eventId) {
        postService.deletePost(eventId);
        return ResponseEntity.ok("Xóa thành công");
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
