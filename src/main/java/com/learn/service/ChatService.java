package com.learn.service;

import com.learn.dto.ChatRequest;
import com.learn.dto.ExternalChatRequest;
import com.learn.response.ChatResponse;
import com.learn.response.ChatSessionResponse;
import com.learn.response.CommonResponse;
import com.learn.response.ExternalChatResponse;
import com.learn.response.PaginationResponse;

public interface ChatService {

    /**
     * Send a message to the company's chatbot (internal — JWT auth).
     */
    ChatResponse chat(Long userId, Long companyId, ChatRequest request);

    /**
     * Send a message via external API key auth.
     * Sessions are identified by a string token (UUID) instead of a DB user ID.
     *
     * @param companyId  resolved from the API key
     * @param request    the external chat request
     */
    ExternalChatResponse externalChat(Long companyId, ExternalChatRequest request);

    /** List all sessions for the authenticated user (paginated). */
    PaginationResponse<ChatSessionResponse> getSessions(
            Long userId, Integer pageNumber, Integer pageSize);

    /** Get a full session with all messages. */
    ChatSessionResponse getSession(Long sessionId, Long userId);

    /** Soft-delete / close a session. */
    CommonResponse closeSession(Long sessionId, Long userId);
}
