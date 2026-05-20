package com.learn.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long sessionId;
    private String sessionTitle;
    private String reply;
}
