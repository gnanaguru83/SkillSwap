package com.skillexchange.session;

import com.skillexchange.common.ApiResponse;
import com.skillexchange.common.PageResponse;
import com.skillexchange.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions")
@Tag(name = "Sessions")
public class SessionController {
    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Book a session")
    public ResponseEntity<ApiResponse<SessionDTO>> book(@AuthenticationPrincipal User user, @Valid @RequestBody BookSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session booked", sessionService.book(user.getId(), request)));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "List upcoming sessions")
    public ResponseEntity<ApiResponse<PageResponse<SessionDTO>>> upcoming(@AuthenticationPrincipal User user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Upcoming sessions listed", sessionService.upcoming(user.getId(), pageable)));
    }

    @GetMapping("/history")
    @Operation(summary = "List past sessions")
    public ResponseEntity<ApiResponse<PageResponse<SessionDTO>>> history(@AuthenticationPrincipal User user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Session history listed", sessionService.history(user.getId(), pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session detail")
    public ResponseEntity<ApiResponse<SessionDTO>> get(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Session detail", sessionService.get(user.getId(), id)));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a session")
    public ResponseEntity<ApiResponse<SessionDTO>> cancel(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Session cancelled", sessionService.cancel(user.getId(), id)));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark session as completed")
    public ResponseEntity<ApiResponse<SessionDTO>> complete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Session completed", sessionService.complete(user.getId(), id)));
    }
}
