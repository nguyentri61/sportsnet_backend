package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.auth.VerifyRequest;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.OTP;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.OTPRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OTPService {
    private final OTPRepository otpRepository;
    private final MailService mailService;
    private final AccountRepository accountRepository;
    @Transactional
    public OTP createOTP(Account account) {
        OTP otp = otpRepository.findByAccount_email(account.getEmail())
                .orElseGet(OTP::new);

        otp.setAccount(account);
        otp.setCode(otp.generateNumericOtp());
        otp.setExpirationTime(Instant.now().plus(30, ChronoUnit.MINUTES));

        otp = otpRepository.save(otp); // Hibernate sẽ UPDATE nếu entity có id
        mailService.sendOtpEmail(account.getEmail(), otp.getCode());
        return otp;
    }



    public Account verifyOTP(VerifyRequest request) {
//        Account account = accountRepository.findByEmail(request.getEmail()).orElseThrow(()-> new InvalidDataException("Account not found"));
        OTP otp = otpRepository.findByAccount_email(request.getEmail()).orElseThrow(() -> new InvalidDataException("OTP not found"));
        if(!Objects.equals(otp.getCode(), request.getOtp()))
        {
            otpRepository.delete(otp);
            throw new InvalidDataException("OTP không khớp!");
        }
        if (otp.getExpirationTime().isBefore(Instant.now())){
            otpRepository.delete(otp);
            throw new InvalidDataException("OTP đã hết hạn");
        }
        Account account = otp.getAccount();
        account.setVerified(true);
        accountRepository.save(account);
        otpRepository.delete(otp);
        return account;
    }
}
