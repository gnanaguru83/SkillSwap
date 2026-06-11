package com.skillexchange.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class AddSkillRequest {
    private UUID skillId;
    @Size(max = 255)
    private String name;
    @Size(max = 255)
    private String category;
    @Size(max = 2000)
    private String skillDescription;
    @NotNull
    private SkillType type;
    @NotNull
    private ProficiencyLevel proficiencyLevel;
    @NotBlank
    @Size(max = 2000)
    private String description;
}
