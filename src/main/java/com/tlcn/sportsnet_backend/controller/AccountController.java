package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.ApiResponse;
import com.tlcn.sportsnet_backend.dto.account.AccountRegisterRequest;
import com.tlcn.sportsnet_backend.dto.account.AccountResponse;
import com.tlcn.sportsnet_backend.dto.account.UpdateProfileRequest;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.service.AccountService;
import com.tlcn.sportsnet_backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<?> getAccount() {
        return ResponseEntity.ok(accountService.getAccount());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(accountService.updateProfile(request));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String uploadedFile = fileStorageService.storeFile(file, "/avatar");

        return ResponseEntity.ok()
                .body(ApiResponse.success(Map.of("fileName", uploadedFile)));
    }

}
