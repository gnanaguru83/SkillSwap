package com.skillexchange.session;

import com.skillexchange.common.AuditableEntity;
import com.skillexchange.matching.MatchRequest;
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

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sessions")
public class Session extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchRequest match;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    @Column(name = "meeting_link")
    private String meetingLink;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;
    private String notes;
    @Builder.Default
    @Column(name = "reminder_sent")
    private boolean reminderSent = false;
    @Builder.Default
    @Column(name = "rating_prompt_sent")
    private boolean ratingPromptSent = false;
}
