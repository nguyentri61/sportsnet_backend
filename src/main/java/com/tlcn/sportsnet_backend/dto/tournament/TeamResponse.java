package com.tlcn.sportsnet_backend.dto.tournament;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {
    String id;
    String teamName;
    String avatarUrl1;
    String avatarUrl2;
    String slug1;
    String slug2;
}
