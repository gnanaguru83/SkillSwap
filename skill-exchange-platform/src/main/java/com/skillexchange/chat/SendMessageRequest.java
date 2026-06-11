package com.skillexchange.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {
    @NotNull
    private UUID receiverId;
    private UUID sessionId;
    @NotBlank
    @Size(max = 5000)
    private String content;
}
