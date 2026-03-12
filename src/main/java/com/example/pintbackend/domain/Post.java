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

package com.example.pintbackend.domain;

import com.example.pintbackend.domain.common.BaseEntity;
import com.example.pintbackend.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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

    // 수정 method in post entity
    public void update(String description, String location, String imageFileS3Key, String filterFileS3Key) {
        /**
         * 수정된 필드들만 바꾸기
         */
        if (description != null) this.description = description;
        if (location != null) this.location = location;
        if (imageFileS3Key != null) this.imageFileS3Key = imageFileS3Key;
        if (filterFileS3Key != null) this.filterFileS3Key = filterFileS3Key;
    }

    public void assignUser(User user) {
        this.user = user;
    }
}
