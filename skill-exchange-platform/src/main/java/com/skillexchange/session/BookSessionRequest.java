package com.skillexchange.session;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookSessionRequest {
    @NotNull
    private UUID matchId;
    @NotNull
    private UUID teacherId;
    @NotNull
    private UUID learnerId;
    @NotNull
    private UUID skillId;
    @NotNull
    private LocalDateTime scheduledAt;
    @NotNull
    @Min(15)
    @Max(480)
    private Integer durationMinutes;
    @Size(max = 500)
    private String meetingLink;
    @Size(max = 2000)
    private String notes;
}
