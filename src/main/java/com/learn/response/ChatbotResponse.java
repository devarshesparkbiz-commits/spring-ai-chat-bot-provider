package com.learn.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {

    private Long chatbotId;
    private String chatbotName;
    private String modelName;
    private Double temperature;
    private Integer topK;
    private Double topP;
    private String systemPrompt;
    private Boolean active;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
