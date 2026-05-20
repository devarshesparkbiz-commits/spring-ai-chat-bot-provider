package com.learn.service;

import com.learn.dto.ChatbotRequest;
import com.learn.response.ChatbotResponse;
import com.learn.response.CommonResponse;

import java.util.List;

public interface ChatbotService {

    /** SUPER_ADMIN: create a chatbot for a specific company. */
    CommonResponse createChatbot(Long companyId, ChatbotRequest request);

    /** COMPANY_ADMIN: update their own company's chatbot settings. */
    CommonResponse updateChatbot(Long chatbotId, ChatbotRequest request);

    /**
     * COMPANY_ADMIN: create-or-update their chatbot.
     * Creates on first call; updates on subsequent calls.
     */
    CommonResponse upsertChatbot(Long companyId, ChatbotRequest request);

    /** COMPANY_ADMIN: get their own company's chatbot. */
    ChatbotResponse getChatbotByCompany(Long companyId);

    /** SUPER_ADMIN: list all chatbots. */
    List<ChatbotResponse> getAllChatbots();

    /** SUPER_ADMIN / COMPANY_ADMIN: toggle active status. */
    CommonResponse toggleActive(Long chatbotId);
}
