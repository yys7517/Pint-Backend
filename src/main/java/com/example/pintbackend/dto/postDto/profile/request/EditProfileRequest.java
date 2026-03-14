package com.example.pintbackend.dto.postDto.profile.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditProfileRequest {
    private String username;
    private String introduction;
    private String city;

    @Schema(type = "string", format = "binary")
    private MultipartFile profileImage;        // presigned S3 URL
}
