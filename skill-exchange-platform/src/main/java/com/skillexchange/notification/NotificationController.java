package com.skillexchange.notification;

import com.skillexchange.common.ApiResponse;
import com.skillexchange.common.PageResponse;
import com.skillexchange.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List my notifications")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(@AuthenticationPrincipal User user, Pageable pageable) {
        PageResponse<Notification> notifications = notificationService.list(user.getId(), pageable);
        PageResponse<Map<String, Object>> response = PageResponse.<Map<String, Object>>builder()
                .content(notifications.getContent().stream().map(this::toDto).toList())
                .pageNumber(notifications.getPageNumber()).pageSize(notifications.getPageSize())
                .totalElements(notifications.getTotalElements()).totalPages(notifications.getTotalPages())
                .last(notifications.isLast()).build();
        return ResponseEntity.ok(ApiResponse.success("Notifications listed", response));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> read(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        notificationService.markRead(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked read", null));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> readAll(@AuthenticationPrincipal User user) {
        notificationService.markAllRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Notifications marked read", null));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unread(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Unread count", Map.of("count", notificationService.unreadCount(user.getId()))));
    }

    private Map<String, Object> toDto(Notification notification) {
        return Map.of(
                "id", notification.getId(),
                "type", notification.getType(),
                "title", notification.getTitle(),
                "message", notification.getMessage(),
                "read", notification.isRead(),
                "createdAt", notification.getCreatedAt()
        );
    }
}
