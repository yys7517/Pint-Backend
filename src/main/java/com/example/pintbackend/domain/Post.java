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
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column
    private String camera;

    @Column(name = "image", nullable = false)
    private String imageFileS3Key;

    @Column(name = "filter")
    private String filterFileS3Key;

    // 수정 method in post entity
    public void update(String description, String location, String imageFileS3Key, String filterFileS3Key) {
        if (description != null) this.description = description;
        if (location != null) this.location = location;
        if (imageFileS3Key != null) this.imageFileS3Key = imageFileS3Key;
        if (filterFileS3Key != null) this.filterFileS3Key = filterFileS3Key;
    }
}
