package com.skillexchange.chat;

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
public class MessageDTO {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private UUID sessionId;
    private String content;
    private boolean read;
    private LocalDateTime sentAt;
}
