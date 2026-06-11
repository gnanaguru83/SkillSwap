package com.skillexchange.availability;

import com.skillexchange.common.ApiResponse;
import com.skillexchange.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/availability")
@Tag(name = "Availability")
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @GetMapping("/me")
    @Operation(summary = "List my availability slots")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> mine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Availability listed", availabilityService.mine(user.getId()).stream().map(this::toDto).toList()));
    }

    @PostMapping
    @Operation(summary = "Add an availability slot")
    public ResponseEntity<ApiResponse<Map<String, Object>>> add(@AuthenticationPrincipal User user, @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Availability added", toDto(availabilityService.add(user.getId(), request))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove an availability slot")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        availabilityService.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Availability removed", null));
    }

    private Map<String, Object> toDto(Availability availability) {
        return Map.of(
                "id", availability.getId(),
                "dayOfWeek", availability.getDayOfWeek(),
                "startTime", availability.getStartTime(),
                "endTime", availability.getEndTime(),
                "timezone", availability.getTimezone()
        );
    }
}
