package com.skillexchange.session;

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
public class SessionDTO {
    private UUID id;
    private UUID matchId;
    private UUID teacherId;
    private String teacherName;
    private UUID learnerId;
    private String learnerName;
    private UUID skillId;
    private String skillName;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String meetingLink;
    private SessionStatus status;
    private String notes;
}
