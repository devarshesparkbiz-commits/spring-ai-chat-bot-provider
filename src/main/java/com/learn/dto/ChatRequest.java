package com.learn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * The user's message / question.
     */
    @NotBlank(message = "Message cannot be blank")
    private String message;

    /**
     * Optional: continue an existing session.
     * If null a new session is created automatically.
     */
    private Long sessionId;
}
