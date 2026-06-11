package com.skillexchange.rating;

import com.skillexchange.common.ApiResponse;
import com.skillexchange.common.PageResponse;
import com.skillexchange.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RatingDTO>> submitRating(
            @AuthenticationPrincipal User currentUser,
            @RequestBody SubmitRatingRequest request) {
        RatingDTO dto = ratingService.submit(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Rating submitted", dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<RatingDTO>>> getRatingsForUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<RatingDTO> ratings = ratingService.userRatings(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Ratings retrieved", ratings));
    }

    // NOTE: Badge endpoint removed — served by UserController at GET
    // /api/v1/users/me/badges
}
