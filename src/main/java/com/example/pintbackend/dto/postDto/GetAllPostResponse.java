package com.example.pintbackend.dto.postDto;

import java.util.List;

public record GetAllPostResponse(
        List<PostImageResponse> postList,
        int page,
        int size,
        boolean hasNext,
        boolean hasPrevious,
        boolean isFirst,
        boolean isLast
) {}
