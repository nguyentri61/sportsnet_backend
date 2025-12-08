package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "otps")
public class OTP {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    String id;

    String code;

    Instant expirationTime;
    @OneToOne(mappedBy = "otp")
    private Account account;
    /**
     * Tự động sinh mã OTP (chỉ gồm số) và đặt thời gian hết hạn 30 phút khi tạo mới
     */
    @PrePersist
    public void onCreate() {
        if (this.code == null) {
            this.code = generateNumericOtp(); // OTP 8 chữ số
        }

        if (this.expirationTime == null) {
            this.expirationTime = Instant.now().plus(30, ChronoUnit.MINUTES);
        }
    }


    /**
     * Sinh OTP chỉ gồm các chữ số (0–9)
     */
    public String generateNumericOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10); // 0-9
            sb.append(digit);
        }

        return sb.toString();
    }
}
