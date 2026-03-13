/**
 * File: null.java
 * Path: com.example.pintbackend.service
 * <p>
 * Outline:
 * counting how many likes a post has
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.service;

import com.example.pintbackend.domain.Post;
import com.example.pintbackend.domain.PostLike;
import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.dto.postDto.LikeResponse;
import com.example.pintbackend.repository.PostLikeRepository;
import com.example.pintbackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    /**
     * 라이크 토글: 포스트 라이크 하면 불러오고, 라이크 안하면 안불러옴.
     * Transactional 이유: writes to DB (insert or delete)
     * takes the post ID and the User entity passed from controller via @AuthenticationPrincipal
     */

    @Transactional
    public LikeResponse toggleLike(Long postId, User user) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("ERROR: 포스트가 없습니다"));

        Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);

        // check if postlike row already exists
        boolean isLiked;

        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            isLiked = false;
        } else {
            PostLike postLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(postLike);
            isLiked = true;
        }
        long likeCount = postLikeRepository.countByPost(post);

        return new LikeResponse(isLiked, likeCount);
    }

    /**
     * 단일 포스트: likeCount + isLiked
     */
    public LikeResponse getLikeInfo(Post post, User user) {
        long likeCount = postLikeRepository.countByPost(post);
        boolean isLiked = (user != null) && postLikeRepository.existsByPostAndUser(post, user);
        return new LikeResponse(isLiked, likeCount);
    }

    /**
     * 다수 포스트: batch query 써서 N + 1 방지
     */
    public Map<Long, Long> getLikeCountByPostIds(List<Long> postIds) {
        return postLikeRepository.countByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
    }

    /**
     * 하나의 batch 쿼리 returns set of post ids the user liked.
     * 유저 라이크 페이지 
     */
    public Set<Long> getLikedPostIds(List<Long> postIds, User user) {
        if (user ==null) {
            return Set.of();
        }
        return postLikeRepository.findLikedPostIdsByUser(postIds, user);
    }


}
