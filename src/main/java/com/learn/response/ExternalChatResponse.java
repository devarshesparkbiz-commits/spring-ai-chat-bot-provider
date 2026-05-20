package com.learn.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalChatResponse {

    private String sessionId;
    private String reply;
    private String chatbotName;
}
