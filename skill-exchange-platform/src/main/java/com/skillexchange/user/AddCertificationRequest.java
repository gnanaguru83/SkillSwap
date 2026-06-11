package com.skillexchange.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class AddCertificationRequest {
    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String issuer;

    @Min(1950)
    @Max(2100)
    private Integer year;

    @Size(max = 500)
    private String credentialUrl;

    /** Optional: the skill this certification backs. */
    private UUID skillId;
}
