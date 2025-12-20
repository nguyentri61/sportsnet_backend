package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.chatbot.ChatbotRequest;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CHATBOT_URL = "http://127.0.0.1:8000/api/chat";

    @PostMapping("/ask")
    public ResponseEntity<?> askChatbot(@RequestBody ChatbotRequest request) {

        // Gửi request sang FastAPI
        ResponseEntity<?> response =
                restTemplate.postForEntity(
                        CHATBOT_URL,
                        request,
                        Object.class
                );

        return ResponseEntity.ok(response.getBody());
    }

}
