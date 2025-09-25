package com.tlcn.sportsnet_backend.dto.reputationHistory;

import com.tlcn.sportsnet_backend.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReputationHistoryResponse {
    String id;
    int change;
    String reason;
    Instant createdAt;
}
