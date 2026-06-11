package com.skillexchange.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String bio;
    private String profilePictureUrl;
    private String location;
    private String linkedinUrl;
}
