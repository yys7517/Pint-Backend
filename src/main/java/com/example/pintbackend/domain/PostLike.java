/**
 * File: null.java
 * Path: com.example.pintbackend.domain
 * <p>
 * Outline:
 * post like entity to join with users and posts
 *
 * uniqueConstraint 게시글에 유저가 라이크 누를수있는 회수가 1.
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.domain;

import com.example.pintbackend.domain.post.Post;
import com.example.pintbackend.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
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
