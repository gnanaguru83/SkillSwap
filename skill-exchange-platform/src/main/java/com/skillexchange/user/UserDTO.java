package com.skillexchange.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String bio;
    private String profilePictureUrl;
    private String location;
    private String linkedinUrl;
    private String headline;
    private String languages;
    private Double averageRating;
    private Long ratingCount;
    private Long totalSessions;
}
