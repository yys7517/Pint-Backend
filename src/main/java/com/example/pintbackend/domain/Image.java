/**
 * domain/Image.java
 * 용도: 이미지 entity 구현,
 * 필드: filename, s3Key, size, width, height, createdAt, contentDescription
 * <p>
 * Last Updated: Junsung Kim
 */

package com.example.pintbackend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "images")
@Getter
@Setter
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private Long size;

}
