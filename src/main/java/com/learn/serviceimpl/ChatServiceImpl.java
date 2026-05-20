package com.learn.serviceimpl;

import com.learn.dto.ChatRequest;
import com.learn.dto.ExternalChatRequest;
import com.learn.entity.*;
import com.learn.enums.MessageRole;
import com.learn.repository.*;
import com.learn.response.*;
import com.learn.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatbotRepository     chatbotRepository;
    private final RagDocumentRepository ragDocumentRepository;
    private final FaqRepository         faqRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository        userRepository;

    // Spring AI Ollama API (low-level) – used to build per-request ChatClient
    private final OllamaApi ollamaApi;

    // ── Mapper helpers ────────────────────────────────────────────────────────

    private ChatMessageResponse toMessageResponse(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .messageId(msg.getMessageId())
                .role(msg.getRole())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .build();
    }

    private ChatSessionResponse toSessionResponse(ChatSession session, boolean includeMessages) {
        List<ChatMessageResponse> messages = includeMessages
                ? chatMessageRepository
                        .findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId())
                        .stream()
                        .map(this::toMessageResponse)
                        .toList()
                : List.of();

        return ChatSessionResponse.builder()
                .sessionId(session.getSessionId())
                .sessionTitle(session.getSessionTitle())
                .active(session.getActive())
                .chatbotId(session.getChatbot().getChatbotId())
                .chatbotName(session.getChatbot().getChatbotName())
                .userId(session.getUser().getUserId())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .messages(messages)
                .build();
    }

    // ── Chat ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ChatResponse chat(Long userId, Long companyId, ChatRequest request) {

        Chatbot chatbot = chatbotRepository.findByCompany_CompanyId(companyId)
                .orElseThrow(() -> new RuntimeException("No chatbot configured for your company"));

        if (!Boolean.TRUE.equals(chatbot.getActive())) {
            throw new RuntimeException("The chatbot is currently inactive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatSession session;
        if (request.getSessionId() != null) {
            session = chatSessionRepository
                    .findBySessionIdAndUser_UserId(request.getSessionId(), userId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            if (!Boolean.TRUE.equals(session.getActive())) {
                throw new RuntimeException("This session is closed");
            }
        } else {
            String title = request.getMessage().length() > 60
                    ? request.getMessage().substring(0, 60) + "…"
                    : request.getMessage();
            session = ChatSession.builder()
                    .sessionTitle(title)
                    .active(true)
                    .chatbot(chatbot)
                    .user(user)
                    .build();
            chatSessionRepository.save(session);
        }

        String reply = processMessage(session, chatbot, companyId, request.getMessage());

        return ChatResponse.builder()
                .sessionId(session.getSessionId())
                .sessionTitle(session.getSessionTitle())
                .reply(reply)
                .build();
    }

    // ── External chat (API key) ───────────────────────────────────────────────

    @Override
    @Transactional
    public ExternalChatResponse externalChat(Long companyId, ExternalChatRequest request) {

        Chatbot chatbot = chatbotRepository.findByCompany_CompanyId(companyId)
                .orElseThrow(() -> new RuntimeException("No chatbot configured for this company"));

        if (!Boolean.TRUE.equals(chatbot.getActive())) {
            throw new RuntimeException("The chatbot is currently inactive");
        }

        ChatSession session;
        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            session = chatSessionRepository
                    .findByExternalSessionTokenAndActiveTrue(request.getSessionId())
                    .orElseThrow(() -> new RuntimeException("Session not found or expired"));
        } else {
            String token = UUID.randomUUID().toString();
            String title = request.getMessage().length() > 60
                    ? request.getMessage().substring(0, 60) + "…"
                    : request.getMessage();
            session = ChatSession.builder()
                    .sessionTitle(title)
                    .externalSessionToken(token)
                    .active(true)
                    .chatbot(chatbot)
                    .build();
            chatSessionRepository.save(session);
        }

        String reply = processMessage(session, chatbot, companyId, request.getMessage());

        return ExternalChatResponse.builder()
                .sessionId(session.getExternalSessionToken())
                .reply(reply)
                .chatbotName(chatbot.getChatbotName())
                .build();
    }

    // ── Session management ────────────────────────────────────────────────────

    @Override
    public PaginationResponse<ChatSessionResponse> getSessions(
            Long userId, Integer pageNumber, Integer pageSize) {

        Page<ChatSession> page = chatSessionRepository
                .findByUser_UserIdAndActiveTrue(userId, PageRequest.of(pageNumber, pageSize));

        List<ChatSessionResponse> data = page.getContent()
                .stream()
                .map(s -> toSessionResponse(s, false))
                .toList();

        return PaginationResponse.<ChatSessionResponse>builder()
                .data(data)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    @Override
    public ChatSessionResponse getSession(Long sessionId, Long userId) {
        ChatSession session = chatSessionRepository
                .findBySessionIdAndUser_UserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return toSessionResponse(session, true);
    }

    @Override
    @Transactional
    public CommonResponse closeSession(Long sessionId, Long userId) {
        ChatSession session = chatSessionRepository
                .findBySessionIdAndUser_UserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setActive(false);
        chatSessionRepository.save(session);
        return CommonResponse.builder()
                .message("Session closed successfully")
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Core message processing: persist user message, call Ollama, persist reply.
     * Shared by both internal (JWT) and external (API key) chat flows.
     */
    private String processMessage(ChatSession session, Chatbot chatbot,
                                  Long companyId, String userText) {
        // Persist user message
        ChatMessage userMsg = ChatMessage.builder()
                .role(MessageRole.USER)
                .content(userText)
                .session(session)
                .build();
        chatMessageRepository.save(userMsg);

        // Build RAG context
        String ragContext = buildRagContext(companyId);

        // Conversation history
        List<ChatMessage> history = chatMessageRepository
                .findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId());

        // Compose prompt
        String fullPrompt = composePrompt(chatbot, ragContext, history, userText);

        // Call Ollama
        OllamaOptions options = OllamaOptions.builder()
                .model(chatbot.getModelName())
                .temperature(chatbot.getTemperature())
                .topK(chatbot.getTopK())
                .topP(chatbot.getTopP())
                .build();

        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();

        String reply = ChatClient.builder(chatModel).build()
                .prompt()
                .user(fullPrompt)
                .call()
                .content();

        // Persist assistant reply
        chatMessageRepository.save(ChatMessage.builder()
                .role(MessageRole.ASSISTANT)
                .content(reply)
                .session(session)
                .build());

        return reply;
    }

    /**
     * Builds a RAG context block from active FAQs and active RAG documents
     * belonging to the company.
     */
    private String buildRagContext(Long companyId) {
        StringBuilder sb = new StringBuilder();

        // FAQs
        List<Faq> faqs = faqRepository.findByCompany_CompanyId(companyId)
                .stream()
                .filter(f -> Boolean.TRUE.equals(f.getActive()))
                .toList();

        if (!faqs.isEmpty()) {
            sb.append("=== Company FAQs ===\n");
            for (Faq faq : faqs) {
                sb.append("Q: ").append(faq.getQuestion()).append("\n");
                sb.append("A: ").append(faq.getAnswer()).append("\n\n");
            }
        }

        // RAG documents
        List<RagDocument> docs = ragDocumentRepository
                .findByCompany_CompanyIdAndActiveTrue(companyId);

        if (!docs.isEmpty()) {
            sb.append("=== Knowledge Base ===\n");
            for (RagDocument doc : docs) {
                sb.append("--- ").append(doc.getDocumentTitle()).append(" ---\n");
                sb.append(doc.getContent()).append("\n\n");
            }
        }

        return sb.toString();
    }

    /**
     * Composes the full prompt sent to the model, including:
     * - System persona / instructions
     * - RAG context
     * - Conversation history (last 10 turns to stay within context window)
     * - The current user message
     */
    private String composePrompt(Chatbot chatbot,
                                 String ragContext,
                                 List<ChatMessage> history,
                                 String currentMessage) {

        StringBuilder sb = new StringBuilder();

        // System prompt / persona
        if (chatbot.getSystemPrompt() != null && !chatbot.getSystemPrompt().isBlank()) {
            sb.append("SYSTEM INSTRUCTIONS:\n")
              .append(chatbot.getSystemPrompt())
              .append("\n\n");
        } else {
            sb.append("SYSTEM INSTRUCTIONS:\n")
              .append("You are ").append(chatbot.getChatbotName())
              .append(", a helpful AI assistant. ")
              .append("Answer questions accurately based on the provided context. ")
              .append("If the answer is not in the context, say so politely.\n\n");
        }

        // RAG context
        if (!ragContext.isBlank()) {
            sb.append("CONTEXT KNOWLEDGE:\n")
              .append(ragContext)
              .append("\n");
        }

        // Conversation history (last 10 messages, excluding the one just saved)
        List<ChatMessage> recent = history.size() > 11
                ? history.subList(history.size() - 11, history.size() - 1)
                : history.subList(0, Math.max(0, history.size() - 1));

        if (!recent.isEmpty()) {
            sb.append("CONVERSATION HISTORY:\n");
            for (ChatMessage msg : recent) {
                String roleLabel = msg.getRole() == MessageRole.USER ? "User" : "Assistant";
                sb.append(roleLabel).append(": ").append(msg.getContent()).append("\n");
            }
            sb.append("\n");
        }

        // Current question
        sb.append("User: ").append(currentMessage).append("\nAssistant:");

        return sb.toString();
    }
}
