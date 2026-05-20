package com.learn.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequest {

    @NotBlank(message = "Chatbot name is required")
    private String chatbotName;

    /**
     * Ollama model identifier. Defaults to "gpt-oss:120b-cloud" if not provided.
     */
    private String modelName;

    @DecimalMin(value = "0.0", message = "Temperature must be >= 0.0")
    @DecimalMax(value = "2.0", message = "Temperature must be <= 2.0")
    private Double temperature;

    @Min(value = 1, message = "Top-K must be >= 1")
    @Max(value = 200, message = "Top-K must be <= 200")
    private Integer topK;

    @DecimalMin(value = "0.0", message = "Top-P must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Top-P must be <= 1.0")
    private Double topP;

    private String systemPrompt;

    private Boolean active;
}
