package com.learn.controller;

import com.learn.dto.ExternalChatRequest;
import com.learn.entity.ApiKey;
import com.learn.response.ExternalChatResponse;
import com.learn.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Public external API for embedding the chatbot in third-party products.
 * Authentication: X-API-Key header (validated by ApiKeyAuthenticationFilter).
 *
 * Base path: /api/v1
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExternalChatController {

    private final ChatService chatService;

    /**
     * Send a message to the company's chatbot.
     *
     * POST /api/v1/chat
     *
     * Headers:
     *   X-API-Key: sk_live_...
     *
     * Request body:
     * {
     *   "message": "What is your return policy?",
     *   "sessionId": "optional-uuid-to-continue-conversation"
     * }
     *
     * Response:
     * {
     *   "sessionId": "uuid-for-this-conversation",
     *   "reply": "Our return policy is...",
     *   "chatbotName": "Support Bot"
     * }
     */
    @PostMapping("/chat")
    public ExternalChatResponse chat(
            @Valid @RequestBody ExternalChatRequest request,
            Authentication auth
    ) {
        ApiKey apiKey = (ApiKey) auth.getDetails();
        Long companyId = apiKey.getCompany().getCompanyId();
        return chatService.externalChat(companyId, request);
    }
}
