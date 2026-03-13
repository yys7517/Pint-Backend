package com.example.pintbackend.dto.postDto;

import java.util.List;

public record GetAllPostResponse(
    List<PostImageResponse> postList
) {

}
