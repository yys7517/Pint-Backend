/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto
 * <p>
 * Outline:
 * current sessioned users profile
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto.profile;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.dto.postDto.LikeResponse;
import com.example.pintbackend.dto.postDto.PostResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
@AllArgsConstructor
public class MyProfileResponse {

    private Long id;
    private String username;
    private String introduction;
    private String city;
    private String email;
    private Boolean isMe;
    private String imageUrl;

    private List<PostResponse> postResponseList;
    private List<LikeResponse> likeResponseList;

    public static MyProfileResponse from(User user, String imageUrl, Boolean isMe) {
        return MyProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .introduction(user.getIntroduction())
                .city(user.getCity())
                .email(user.getEmail())
                .isMe(isMe)
                .imageUrl(imageUrl)
                .build();

    }
}
