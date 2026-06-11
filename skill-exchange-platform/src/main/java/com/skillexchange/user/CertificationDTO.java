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
public class CertificationDTO {
    private UUID id;
    private String title;
    private String issuer;
    private Integer year;
    private String credentialUrl;
    private UUID skillId;
    private String skillName;
}
