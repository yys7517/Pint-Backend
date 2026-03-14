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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
@AllArgsConstructor
public class MyProfileResponse {

    private String username;
    private String introduction;
    private String city;
    private String email;
    private Boolean isMe;

    // Jackson  이 변수를 JSON Key 로 만든다
    private List<ProfileImageResponse> postList;        // { postId, imageUrl}
    private List<ProfileImageResponse> likedPostList;

    public static MyProfileResponse from(User user,Boolean isMe,
                                         List<ProfileImageResponse> postList, List<ProfileImageResponse> likedPostList) {
        return MyProfileResponse.builder()
                .username(user.getUsername())
                .introduction(user.getIntroduction())
                .city(user.getCity())
                .email(user.getEmail())
                .isMe(isMe)
                .postList(postList)
                .likedPostList(likedPostList)
                .build();

    }
}
