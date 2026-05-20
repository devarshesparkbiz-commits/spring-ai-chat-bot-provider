package com.learn.controller;

import com.learn.dto.ChatRequest;
import com.learn.response.ChatResponse;
import com.learn.response.ChatSessionResponse;
import com.learn.response.CommonResponse;
import com.learn.response.PaginationResponse;
import com.learn.service.ChatService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Chat endpoints for COMPANY_USER.
 * All routes require a valid JWT with COMPANY_USER role.
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long getUserIdFromAuth(Authentication auth) {
        if (auth.getDetails() instanceof Claims claims) {
            Object raw = claims.get("userId");
            if (raw == null) throw new RuntimeException("No userId in token");
            return ((Number) raw).longValue();
        }
        throw new RuntimeException("Unable to resolve userId from token");
    }

    private Long getCompanyIdFromAuth(Authentication auth) {
        if (auth.getDetails() instanceof Claims claims) {
            Object raw = claims.get("companyId");
            if (raw == null) throw new RuntimeException("No companyId in token");
            return ((Number) raw).longValue();
        }
        throw new RuntimeException("Unable to resolve companyId from token");
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Send a message to the company chatbot.
     * Omit sessionId to start a new conversation; include it to continue an existing one.
     *
     * POST /chat/message
     */
    @PostMapping("/message")
    public ChatResponse sendMessage(
            @Valid @RequestBody ChatRequest request,
            Authentication auth
    ) {
        Long userId    = getUserIdFromAuth(auth);
        Long companyId = getCompanyIdFromAuth(auth);
        return chatService.chat(userId, companyId, request);
    }

    /**
     * List all active chat sessions for the authenticated user (paginated).
     *
     * GET /chat/sessions
     */
    @GetMapping("/sessions")
    public PaginationResponse<ChatSessionResponse> getSessions(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Authentication auth
    ) {
        Long userId = getUserIdFromAuth(auth);
        return chatService.getSessions(userId, pageNumber, pageSize);
    }

    /**
     * Get a specific session with its full message history.
     *
     * GET /chat/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId:\\d+}")
    public ChatSessionResponse getSession(
            @PathVariable Long sessionId,
            Authentication auth
    ) {
        Long userId = getUserIdFromAuth(auth);
        return chatService.getSession(sessionId, userId);
    }

    /**
     * Close / archive a chat session.
     *
     * DELETE /chat/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId:\\d+}")
    public CommonResponse closeSession(
            @PathVariable Long sessionId,
            Authentication auth
    ) {
        Long userId = getUserIdFromAuth(auth);
        return chatService.closeSession(sessionId, userId);
    }
}
