package com.skillexchange.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 255)
    private String fullName;
    @Size(max = 5000)
    private String bio;
    @Size(max = 500)
    private String profilePictureUrl;
    @Size(max = 255)
    private String location;
    @Size(max = 500)
    private String linkedinUrl;
    @Size(max = 255)
    private String headline;
    @Size(max = 500)
    private String languages;
}
