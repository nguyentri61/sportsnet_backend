package com.tlcn.sportsnet_backend.dto.tournament_history;

import com.tlcn.sportsnet_backend.enums.TournamentFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryInfoResponse {
    String categoryId;
    String categoryName;   // Đơn nam, Đôi nam...
    String type;           // SINGLE / DOUBLE
    Double minLevel;
    Double maxLevel;
}
