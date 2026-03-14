/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto
 * <p>
 * Outline:
 * user like response
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto;


public record LikeResponse(boolean isLiked,
                           int likeCount) {
}
