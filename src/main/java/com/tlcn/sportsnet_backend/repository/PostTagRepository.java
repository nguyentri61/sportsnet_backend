package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, String> {
    List<PostTag> findByPostId(String postId);
}
