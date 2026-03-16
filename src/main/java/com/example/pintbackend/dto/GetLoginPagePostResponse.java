package com.example.pintbackend.dto;

import java.util.List;

public record GetLoginPagePostResponse(
        List<LoginPagePostResponse> postList
) {

}
