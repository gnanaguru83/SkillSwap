package com.skillexchange.rating;

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
public class RatingDTO {
    private UUID id;
    private UUID sessionId;
    private UUID raterId;
    private String raterName;
    private UUID ratedUserId;
    private String ratedUserName;
    private Integer score;
    private String feedback;
    private LocalDateTime createdAt;
}
