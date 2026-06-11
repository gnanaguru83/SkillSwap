package com.skillexchange.matching;

import com.skillexchange.skill.SkillDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchSuggestionDTO {
    private UUID userId;
    private String fullName;
    private String profilePicture;
    private List<SkillDTO> teachSkills;
    private List<SkillDTO> learnSkills;
    private int compatibilityScore;
    private double averageRating;
    private List<String> commonAvailability;
    private String location;
    private boolean online;
    private String aiMatchReason;
}
