package com.example.pintbackend.repository;

import com.example.pintbackend.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    int countByPostId(Long postId);

    // TODO. 내 프로필 좋아요 목록
    @Query("""
          SELECT pl.post.id
          FROM PostLike pl
          WHERE pl.user.id = :userId
          """)
    Set<Long> findAllLikedPostIdsByUserId(@Param("userId") Long userId);
}
