package com.skillexchange.matching;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestDTO {
    private UUID id;
    private UUID requesterId;
    private String requesterName;
    private UUID targetId;
    private String targetName;
    private UUID teachSkillId;
    private String teachSkillName;
    private UUID learnSkillId;
    private String learnSkillName;
    private MatchStatus status;
    private String message;
    private Integer compatibilityScore;
    private LocalDateTime createdAt;
}
