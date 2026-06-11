package com.skillexchange.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {
    private UUID id;
    private String name;
    private String category;
    private String description;
    private SkillType type;
    private ProficiencyLevel proficiencyLevel;
    private boolean active;
}
