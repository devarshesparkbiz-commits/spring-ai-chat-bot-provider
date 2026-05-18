package com.learn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AIChatController {

    private final ChatClient.Builder chatClientBuilder;

    @GetMapping
    public String chat(@RequestParam String prompt) {

        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}