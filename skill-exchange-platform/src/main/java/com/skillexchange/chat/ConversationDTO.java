package com.skillexchange.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ConversationDTO {
    private UUID partnerId;
    private String partnerEmail;
    private String partnerName;
    private String partnerAvatarUrl;
    private boolean partnerOnline;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private boolean lastMessageIsMe;
    private long unreadCount;
}