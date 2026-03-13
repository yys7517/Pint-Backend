package com.example.pintbackend.repository;

import com.example.pintbackend.domain.Post;
import com.example.pintbackend.domain.PostLike;
import com.example.pintbackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostAndUser(Post post, User user);     // used for toggle

    boolean existsByPostAndUser(Post post, User user);              // isLiked (single)

    long countByPost(Post post);                                    // likeCount (single)

    // 모든 포스트 불러올때 쿼리, avoids N + 1
    // likes per post
    @Query("""
                        SELECT pl.post.id, COUNT(pl)
                        FROM PostLike pl
                        WHERE pl.post.id
                        IN :postIds
                        GROUP BY pl.post.id
            """)
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);

    @Query("""
                    SELECT pl.post.id FROM PostLike pl
                    WHERE pl.post.id
                    IN :postIds
                    AND pl.user = :user
            """)
    Set<Long> findLikedPostIdsByUser(@Param("postIds") List<Long> postIds, @Param("user") User user);


}
