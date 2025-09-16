package com.tlcn.sportsnet_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tlcn.sportsnet_backend.enums.MediaTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "post_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String filename;

    // IMAGE hoặc VIDEO
    @Enumerated(EnumType.STRING)
    MediaTypeEnum type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    Post post;
}
