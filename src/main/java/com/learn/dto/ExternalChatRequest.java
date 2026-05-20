package com.learn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalChatRequest {

    @NotBlank(message = "message is required")
    private String message;

    /**
     * Optional: pass a sessionId to continue an existing conversation.
     * Omit (or pass null) to start a new session.
     */
    private String sessionId;
}
