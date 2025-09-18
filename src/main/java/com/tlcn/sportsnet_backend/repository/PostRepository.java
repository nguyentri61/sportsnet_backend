package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    List<Post> findByEventId(String eventId);
}
