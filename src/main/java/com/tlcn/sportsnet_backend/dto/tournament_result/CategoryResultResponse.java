package com.tlcn.sportsnet_backend.dto.tournament_result;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResultResponse {
    String categoryId;
    String categoryName;

    List<CategoryResultItemResponse> results;
}
