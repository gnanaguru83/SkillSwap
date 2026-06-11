package com.skillexchange.chat;

import com.skillexchange.auth.JwtService;
import com.skillexchange.common.ApiResponse;
import com.skillexchange.common.PageResponse;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Tag(name = "Chat")
public class ChatController {
    private final ChatService chatService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @MessageMapping("/chat.send")
    @Operation(summary = "Send a chat message over WebSocket")
    public void send(@Payload SendMessageRequest request, SimpMessageHeaderAccessor headers) {
        String token = String.valueOf(headers.getFirstNativeHeader("Authorization")).replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        User sender = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        chatService.send(sender.getId(), request);
    }

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api/v1/chat")
    @Tag(name = "Chat")
    public static class RestChatController {
        private final ChatService chatService;

        @GetMapping("/conversations")
        @Operation(summary = "List conversations")
        public ResponseEntity<ApiResponse<List<ConversationDTO>>> conversations(@AuthenticationPrincipal User user) {
            return ResponseEntity.ok(ApiResponse.success("Conversations listed", chatService.conversationSummaries(user.getId())));
        }

        @GetMapping("/messages/{userId}")
        @Operation(summary = "Get message history with a user")
        public ResponseEntity<ApiResponse<PageResponse<MessageDTO>>> history(@AuthenticationPrincipal User user, @PathVariable UUID userId, Pageable pageable) {
            return ResponseEntity.ok(ApiResponse.success("Messages listed", chatService.historyAndMarkRead(user.getId(), userId, pageable)));
        }
    }
}