package com.skillexchange.matching;

import com.skillexchange.common.ApiResponse;
import com.skillexchange.common.PageResponse;
import com.skillexchange.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
@Tag(name = "Matching")
public class MatchController {
    private final MatchingService matchingService;

    @GetMapping("/suggestions")
    @Operation(summary = "Get AI-powered match suggestions")
    public ResponseEntity<ApiResponse<List<MatchSuggestionDTO>>> suggestions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Suggestions found", matchingService.findMatches(user.getId())));
    }

    @PostMapping("/request")
    @Operation(summary = "Send a match request")
    public ResponseEntity<ApiResponse<MatchRequestDTO>> request(@AuthenticationPrincipal User user, @Valid @RequestBody CreateMatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Match request sent", matchingService.request(user.getId(), request.getTargetId(), request.getTeachSkillId(), request.getLearnSkillId(), request.getMessage())));
    }

    @GetMapping("/received")
    @Operation(summary = "List incoming match requests")
    public ResponseEntity<ApiResponse<PageResponse<MatchRequestDTO>>> received(@AuthenticationPrincipal User user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Received requests listed", matchingService.received(user.getId(), pageable)));
    }

    @GetMapping("/sent")
    @Operation(summary = "List outgoing match requests")
    public ResponseEntity<ApiResponse<PageResponse<MatchRequestDTO>>> sent(@AuthenticationPrincipal User user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Sent requests listed", matchingService.sent(user.getId(), pageable)));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept a match request")
    public ResponseEntity<ApiResponse<MatchRequestDTO>> accept(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Match accepted", matchingService.accept(user.getId(), id)));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a match request")
    public ResponseEntity<ApiResponse<MatchRequestDTO>> reject(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Match rejected", matchingService.reject(user.getId(), id)));
    }

    @Data
    public static class CreateMatchRequest {
        @NotNull
        private UUID targetId;
        @NotNull
        private UUID teachSkillId;
        @NotNull
        private UUID learnSkillId;
        @Size(max = 2000)
        private String message;
    }
}
