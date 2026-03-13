/**
 * File: null.java
 * Path: com.example.pintbackend.domain
 * <p>
 * Outline:
 * post like entity to join with users and posts
 *
 * uniqueConstraint 포스트에 유저가 라이크 누를수있는 회수가 1.
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.domain;

import com.example.pintbackend.domain.post.Post;
import com.example.pintbackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
