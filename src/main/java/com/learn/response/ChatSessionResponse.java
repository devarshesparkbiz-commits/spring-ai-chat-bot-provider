package com.learn.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse {

    private Long sessionId;
    private String sessionTitle;
    private Boolean active;
    private Long chatbotId;
    private String chatbotName;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatMessageResponse> messages;
}
