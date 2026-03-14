/**
 * File: null.java Path: com.example.pintbackend.service
 * <p>
 * Outline: counting how many likes a post has
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.service;


import com.example.pintbackend.domain.PostLike;
import com.example.pintbackend.domain.post.Post;
import com.example.pintbackend.domain.post.exception.PostNotFoundException;
import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.domain.user.exception.UserNotFoundException;
import com.example.pintbackend.dto.postDto.LikeResponse;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.repository.PostLikeRepository;
import com.example.pintbackend.repository.PostRepository;
import com.example.pintbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponse toggleLike(Long postId, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        boolean isLiked;

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(postId)
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userDetails.getEmail())
        );

        if (existing.isPresent()) {
            // on -> off (toggle)
            PostLike existingLike = existing.get();

            post.removeLike(existingLike);
            user.removeLike(existingLike);
            postLikeRepository.delete(existingLike);

            isLiked = false;
        } else {
            // off -> on (toggle)
            PostLike newLike = PostLike.create(post, user);
            postLikeRepository.save(newLike);

            isLiked = true;
        }

        int likeCount = postLikeRepository.countByPostId(postId);

        return new LikeResponse(isLiked, likeCount);
    }

    // TODO. 마이 좋아요 화면: 유저가 좋아요한 게시글 반환. (좋아요 목록 게시글 어떤 특성 필요한지 모름)
//    public List<Post> getMyLikedPosts(CustomUserDetails userDetails) {
//
//        return postLikeRepository.findAllLikedPostIdsByUserId(userDetails.getUserId());
//    }

}
