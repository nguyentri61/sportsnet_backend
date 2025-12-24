package com.tlcn.sportsnet_backend.dto.tournament;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerResponse{
    String id;
    String name;
    String avatarUrl;
    String slug;
    
}
