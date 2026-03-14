/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto
 * <p>
 * Outline:
 * current sessioned users profile
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto.profile.response;

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
    private String profileImageUrl;

    // Jackson  이 변수를 JSON Key 로 만든다
    private List<ProfilePostResponse> postList;        // { postId, imageUrl}
    private List<ProfilePostResponse> likedPostList;

    public static MyProfileResponse from(User user, String profileImageUrl, Boolean isMe,
                                         List<ProfilePostResponse> postList, List<ProfilePostResponse> likedPostList) {
        return MyProfileResponse.builder()
                .username(user.getUsername())
                .introduction(user.getIntroduction())
                .city(user.getCity())
                .email(user.getEmail())
                .isMe(isMe)
                .profileImageUrl(profileImageUrl)
                .postList(postList)
                .likedPostList(likedPostList)
                .build();

    }
}
