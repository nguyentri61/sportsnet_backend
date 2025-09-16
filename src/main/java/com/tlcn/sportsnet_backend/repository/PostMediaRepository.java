package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, String> {
    List<PostMedia> findByPostId(String postId);
}
