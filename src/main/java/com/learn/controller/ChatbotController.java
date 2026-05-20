package com.learn.controller;

import com.learn.dto.ChatbotRequest;
import com.learn.response.ChatbotResponse;
import com.learn.response.CommonResponse;
import com.learn.service.ChatbotService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chatbot configuration endpoints.
 *
 * SUPER_ADMIN  → /chatbot/admin/**
 * COMPANY_ADMIN → /chatbot/my-company/**
 */
@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long getCompanyIdFromAuth(Authentication auth) {
        if (auth.getDetails() instanceof Claims claims) {
            Object raw = claims.get("companyId");
            if (raw == null) throw new RuntimeException("No companyId in token");
            return ((Number) raw).longValue();
        }
        throw new RuntimeException("Unable to resolve companyId from token");
    }

    // ── SUPER_ADMIN endpoints ─────────────────────────────────────────────────

    /**
     * Create a chatbot for a specific company.
     * POST /chatbot/admin/company/{companyId}
     */
    @PostMapping("/admin/company/{companyId:\\d+}")
    public CommonResponse createChatbot(
            @PathVariable Long companyId,
            @Valid @RequestBody ChatbotRequest request
    ) {
        return chatbotService.createChatbot(companyId, request);
    }

    /**
     * List all chatbots across all companies.
     * GET /chatbot/admin/all
     */
    @GetMapping("/admin/all")
    public List<ChatbotResponse> getAllChatbots() {
        return chatbotService.getAllChatbots();
    }

    /**
     * Get chatbot for a specific company.
     * GET /chatbot/admin/company/{companyId}
     */
    @GetMapping("/admin/company/{companyId:\\d+}")
    public ChatbotResponse getChatbotByCompany(@PathVariable Long companyId) {
        return chatbotService.getChatbotByCompany(companyId);
    }

    /**
     * Toggle active status.
     * PATCH /chatbot/admin/{chatbotId}/toggle
     */
    @PatchMapping("/admin/{chatbotId:\\d+}/toggle")
    public CommonResponse toggleActive(@PathVariable Long chatbotId) {
        return chatbotService.toggleActive(chatbotId);
    }

    /**
     * Update any chatbot by ID (super admin override).
     * PUT /chatbot/admin/{chatbotId}
     */
    @PutMapping("/admin/{chatbotId:\\d+}")
    public CommonResponse updateChatbotById(
            @PathVariable Long chatbotId,
            @Valid @RequestBody ChatbotRequest request
    ) {
        return chatbotService.updateChatbot(chatbotId, request);
    }

    // ── COMPANY_ADMIN endpoints ───────────────────────────────────────────────

    /**
     * Get the chatbot for the authenticated company admin's company.
     * GET /chatbot/my-company
     */
    @GetMapping("/my-company")
    public ChatbotResponse getMyChatbot(Authentication auth) {
        Long companyId = getCompanyIdFromAuth(auth);
        return chatbotService.getChatbotByCompany(companyId);
    }

    /**
     * Upsert the chatbot for the authenticated company admin's company.
     * Creates it on first save; updates it on subsequent saves.
     * PUT /chatbot/my-company
     */
    @PutMapping("/my-company")
    public CommonResponse upsertMyChatbot(
            @Valid @RequestBody ChatbotRequest request,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return chatbotService.upsertChatbot(companyId, request);
    }
}
