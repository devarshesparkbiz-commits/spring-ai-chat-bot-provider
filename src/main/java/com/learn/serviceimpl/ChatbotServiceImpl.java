package com.learn.serviceimpl;

import com.learn.dto.ChatbotRequest;
import com.learn.entity.Chatbot;
import com.learn.entity.Company;
import com.learn.repository.ChatbotRepository;
import com.learn.repository.CompanyRepository;
import com.learn.response.ChatbotResponse;
import com.learn.response.CommonResponse;
import com.learn.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private static final String DEFAULT_MODEL      = "gpt-oss:120b-cloud";
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int    DEFAULT_TOP_K       = 40;
    private static final double DEFAULT_TOP_P       = 0.9;

    private final ChatbotRepository  chatbotRepository;
    private final CompanyRepository  companyRepository;

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ChatbotResponse toResponse(Chatbot chatbot) {
        return ChatbotResponse.builder()
                .chatbotId(chatbot.getChatbotId())
                .chatbotName(chatbot.getChatbotName())
                .modelName(chatbot.getModelName())
                .temperature(chatbot.getTemperature())
                .topK(chatbot.getTopK())
                .topP(chatbot.getTopP())
                .systemPrompt(chatbot.getSystemPrompt())
                .active(chatbot.getActive())
                .companyId(chatbot.getCompany().getCompanyId())
                .companyName(chatbot.getCompany().getCompanyName())
                .createdAt(chatbot.getCreatedAt())
                .updatedAt(chatbot.getUpdatedAt())
                .build();
    }

    // ── Operations ────────────────────────────────────────────────────────────

    @Override
    public CommonResponse createChatbot(Long companyId, ChatbotRequest request) {

        if (chatbotRepository.existsByCompany_CompanyId(companyId)) {
            throw new RuntimeException(
                    "A chatbot already exists for this company. Use update instead.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Chatbot chatbot = Chatbot.builder()
                .chatbotName(request.getChatbotName())
                .modelName(request.getModelName() != null
                        ? request.getModelName()
                        : DEFAULT_MODEL)
                .temperature(request.getTemperature() != null
                        ? request.getTemperature()
                        : DEFAULT_TEMPERATURE)
                .topK(request.getTopK() != null
                        ? request.getTopK()
                        : DEFAULT_TOP_K)
                .topP(request.getTopP() != null
                        ? request.getTopP()
                        : DEFAULT_TOP_P)
                .systemPrompt(request.getSystemPrompt())
                .active(request.getActive() != null ? request.getActive() : true)
                .company(company)
                .build();

        chatbotRepository.save(chatbot);

        return CommonResponse.builder()
                .message("Chatbot created successfully")
                .build();
    }

    @Override
    public CommonResponse updateChatbot(Long chatbotId, ChatbotRequest request) {

        Chatbot chatbot = chatbotRepository.findById(chatbotId)
                .orElseThrow(() -> new RuntimeException("Chatbot not found"));

        if (request.getChatbotName() != null) {
            chatbot.setChatbotName(request.getChatbotName());
        }
        if (request.getModelName() != null) {
            chatbot.setModelName(request.getModelName());
        }
        if (request.getTemperature() != null) {
            chatbot.setTemperature(request.getTemperature());
        }
        if (request.getTopK() != null) {
            chatbot.setTopK(request.getTopK());
        }
        if (request.getTopP() != null) {
            chatbot.setTopP(request.getTopP());
        }
        if (request.getSystemPrompt() != null) {
            chatbot.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getActive() != null) {
            chatbot.setActive(request.getActive());
        }

        chatbotRepository.save(chatbot);

        return CommonResponse.builder()
                .message("Chatbot updated successfully")
                .build();
    }

    @Override
    public CommonResponse upsertChatbot(Long companyId, ChatbotRequest request) {
        return chatbotRepository.findByCompany_CompanyId(companyId)
                .map(existing -> updateChatbot(existing.getChatbotId(), request))
                .orElseGet(() -> createChatbot(companyId, request));
    }

    @Override
    public ChatbotResponse getChatbotByCompany(Long companyId) {
        Chatbot chatbot = chatbotRepository.findByCompany_CompanyId(companyId)
                .orElseThrow(() -> new RuntimeException(
                        "No chatbot configured for this company"));
        return toResponse(chatbot);
    }

    @Override
    public List<ChatbotResponse> getAllChatbots() {
        return chatbotRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CommonResponse toggleActive(Long chatbotId) {
        Chatbot chatbot = chatbotRepository.findById(chatbotId)
                .orElseThrow(() -> new RuntimeException("Chatbot not found"));
        chatbot.setActive(!chatbot.getActive());
        chatbotRepository.save(chatbot);
        return CommonResponse.builder()
                .message("Chatbot " + (chatbot.getActive() ? "activated" : "deactivated")
                        + " successfully")
                .build();
    }
}
