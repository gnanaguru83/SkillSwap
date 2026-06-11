package com.skillexchange.matching;

import com.skillexchange.common.AuditableEntity;
import com.skillexchange.skill.Skill;
import com.skillexchange.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "match_requests")
public class MatchRequest extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teach_skill_id", nullable = false)
    private Skill teachSkill;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learn_skill_id", nullable = false)
    private Skill learnSkill;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.PENDING;
    private String message;
    @Column(name = "compatibility_score")
    private Integer compatibilityScore;
}
