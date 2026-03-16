package com.example.pintbackend.repository;

import com.example.pintbackend.domain.post.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 내림차순으로 상위 10개만 가져오기
    List<Post> findTop10ByOrderByCreatedAtDesc();
}
