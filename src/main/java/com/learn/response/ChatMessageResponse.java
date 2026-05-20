package com.learn.response;

import com.learn.enums.MessageRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long messageId;
    private MessageRole role;
    private String content;
    private LocalDateTime createdAt;
}
