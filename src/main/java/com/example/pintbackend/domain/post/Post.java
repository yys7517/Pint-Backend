/**
 * File: null.java
 * Path: com.example.pintbackend.domain
 * <p>
 * Outline:
 * 작성페이지
 * field: imgId, postTitle, imgDescription, filterInfo, created_at
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.domain.post;

import com.example.pintbackend.domain.PostLike;
import com.example.pintbackend.domain.common.BaseEntity;
import com.example.pintbackend.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "description", nullable = false)
    @Size(min=4)
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column
    private String camera;

    @Column
    private Long width;

    @Column
    private Long height;

    @Column(name = "image", nullable = false)
    private String imageFileS3Key;

    @Column(name = "filter")
    private String filterFileS3Key;

  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<PostLike> likes = new ArrayList<>();

    // 수정 method in post entity
    public void update(String description, String location, String camera, String filterFileS3Key) {
        /**
         * 수정된 필드들만 바꾸기
         */
        if (description != null) this.description = description;
        if (location != null) this.location = location;
        if (camera != null) this.camera = camera;
        this.filterFileS3Key = filterFileS3Key;
    }

    public void assignUser(User user) {
        this.user = user;
    }

    public void addLike(PostLike postLike) {
        if (likes.contains(postLike)) {
            return;
        }

        likes.add(postLike);
        postLike.assignPost(this);
    }

    public void removeLike(PostLike postLike) {
        likes.remove(postLike);
        postLike.assignPost(null);
    }
}
