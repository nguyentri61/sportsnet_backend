package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.account.AccountRegisterRequest;
import com.tlcn.sportsnet_backend.dto.account.AccountResponse;
import com.tlcn.sportsnet_backend.dto.account.UpdateProfileRequest;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Role;
import com.tlcn.sportsnet_backend.entity.UserInfo;
import com.tlcn.sportsnet_backend.error.UnauthorizedException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.RoleRepository;
import com.tlcn.sportsnet_backend.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public AccountResponse getAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại"));
        return toResponse(account);
    }

    public Account registerAccount(AccountRegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại, vui lòng chọn email khác.");
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));

        Account newAccount = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .build();

        UserInfo userInfo = UserInfo.builder()
                .fullName(request.getFullName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .address(request.getAddress())
                .account(newAccount)
                .build();
        

        newAccount.setUserInfo(userInfo);
        newAccount = accountRepository.save(newAccount);

        return newAccount;

    }

    public AccountResponse updateProfile(UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại"));

        UserInfo userInfo = account.getUserInfo();
        userInfo.setFullName(request.getFullName());
        userInfo.setGender(request.getGender());
        userInfo.setAddress(request.getAddress());
        userInfo.setPhone(request.getPhone());
        userInfo.setBio(request.getBio());
        userInfo.setBirthDate(request.getBirthDate());
        if(request.getAvatarUrl() != null) {
            userInfo.setAvatarUrl(request.getAvatarUrl());
        }

        account.setUserInfo(userInfo);

        account = accountRepository.save(account);

        return toResponse(account);
    }

    public AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .fullName(account.getUserInfo().getFullName())
                .gender(account.getUserInfo().getGender())
                .address(account.getUserInfo().getAddress())
                .birthDate(account.getUserInfo().getBirthDate())
                .phone(account.getUserInfo().getPhone())
                .bio(account.getUserInfo().getBio())
                .avatarUrl(fileStorageService.getFileUrl(account.getUserInfo().getAvatarUrl(), "/avatar"))
                .enabled(account.isEnabled())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .createdBy(account.getCreatedBy())
                .updatedBy(account.getUpdatedBy())
                .build();
    }
}
