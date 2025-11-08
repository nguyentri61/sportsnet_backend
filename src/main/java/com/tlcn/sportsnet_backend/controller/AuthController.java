package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.ApiResponse;
import com.tlcn.sportsnet_backend.dto.account.AccountRegisterRequest;
import com.tlcn.sportsnet_backend.dto.account.AccountResponse;
import com.tlcn.sportsnet_backend.dto.auth.LoginDTO;
import com.tlcn.sportsnet_backend.dto.auth.VerifyRequest;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.OTP;
import com.tlcn.sportsnet_backend.entity.RefreshToken;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.error.UnauthorizedException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.service.AccountService;
import com.tlcn.sportsnet_backend.service.OTPService;
import com.tlcn.sportsnet_backend.service.RefreshTokenService;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityUtil securityUtil;
    private final AuthenticationManager authenticationManager;
    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;
    private final AccountRepository accountRepository;
    @Value("${jwt.token-verify-validity-in-seconds}")
    private long refreshTokenExpiration;
    @Value("${jwt.token-create-validity-in-seconds}")
    private long expire_access;
    private final OTPService otpService;
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginDTO loginDTO,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest request
    ) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
        Account account = accountService.findByEmail(loginDTO.getEmail())
                        .orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại"));
        try {

            // Xác thực đăng nhập
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // Tạo access token
            String accessToken = securityUtil.createToken(authentication);

            if (deviceId == null || deviceId.isBlank()) {
                deviceId = UUID.randomUUID().toString();
            }

            String refreshToken = refreshTokenService.create(
                    account,
                    deviceId,
                    request.getHeader("User-Agent"),
                    request.getRemoteAddr()
            );

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();

            ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true) // nếu chỉ muốn server-side đọc
                    .secure(true)
                    .path("/")
                    .maxAge(expire_access)
                    .build();
            ResponseCookie deviceIdCookie = ResponseCookie.from("deviceId", deviceId)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, deviceIdCookie.toString())
                    .body(ApiResponse.success(Map.of("accessToken", accessToken, "refreshToken", refreshToken, "deviceId", deviceId)));

        } catch (LockedException e) {
            throw new InvalidDataException("Tài khoản bị khóa");
        } catch (DisabledException e) {
            otpService.createOTP(account);
            throw new InvalidDataException("Tài khoản chưa được kích hoạt");
        } catch (BadCredentialsException e) {
            throw new InvalidDataException("Email hoặc mật khẩu không đúng");
        }
    }

    @GetMapping("/send-otp/{email}")
    public ResponseEntity<?> sendOtp(@PathVariable String email) {
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Account không tồn tại"));
        otpService.createOTP(account);
        return ResponseEntity.ok("Gửi OTP thành công");
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @RequestHeader( "refreshToken") String refreshToken,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        if (refreshToken.isBlank() || refreshToken.isEmpty()) {
            throw new UnauthorizedException("Bạn chưa đăng nhập hoặc thiếu refresh token");
        }

        RefreshToken token = refreshTokenService.verify(refreshToken, deviceId);
        Account account = token.getAccount();

        String newAccessToken = securityUtil.createTokenFromAccount(account);
        String newRefresh = refreshTokenService.create(
                account,
                deviceId,
                token.getUserAgent(),
                token.getIpAddress()
        );

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefresh)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(expire_access)
                .build();

        ResponseCookie deviceIdCookie = ResponseCookie.from("deviceId", deviceId)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deviceIdCookie.toString())
                .body(ApiResponse.success(Map.of("accessToken", newAccessToken, "refreshToken" , newRefresh, "deviceId" , deviceId)));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("refreshToken") String refreshToken,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        refreshTokenService.revoke(refreshToken, deviceId); // tìm theo token + deviceId và xóa

        ResponseCookie clearRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        ResponseCookie clearAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, clearAccessCookie.toString())
                .body(ApiResponse.success("Đăng xuất thành công", null));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody VerifyRequest request,HttpServletRequest headerRequest, @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
      Account account = otpService.verifyOTP(request);
       return ResponseEntity.ok("Xác thực tài khoản thành công");

    }

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody AccountRegisterRequest registerRequest
                                             ) {
        Account account = accountService.registerAccount(registerRequest);
        OTP otp = otpService.createOTP(account);
       return ResponseEntity.ok("Đăng ký thành công");
    }
}
