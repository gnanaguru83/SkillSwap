package com.skillexchange.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDTO {
    private UUID id;
    @NotBlank
    @Size(max = 255)
    private String institution;
    @Size(max = 255)
    private String degree;
    @Size(max = 255)
    private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
}
