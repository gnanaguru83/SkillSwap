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
public class ExperienceDTO {
    private UUID id;
    @NotBlank
    @Size(max = 255)
    private String company;
    @Size(max = 255)
    private String title;
    private Integer startYear;
    private Integer endYear;
    @Size(max = 1000)
    private String description;
}
