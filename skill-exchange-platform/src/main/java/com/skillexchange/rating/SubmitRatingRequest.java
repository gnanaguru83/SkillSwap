package com.skillexchange.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitRatingRequest {
    @NotNull
    private UUID sessionId;
    @NotNull
    private UUID ratedUserId;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;
    @Size(max = 5000)
    private String feedback;
}
