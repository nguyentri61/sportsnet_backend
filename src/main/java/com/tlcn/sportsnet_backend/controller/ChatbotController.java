package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.chatbot.ChatbotRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CHATBOT_URL = "http://127.0.0.1:8000/chat";

    @PostMapping("/ask")
    public ResponseEntity<?> askChatbot(
            @RequestBody ChatbotRequest request,
            HttpServletRequest httpRequest
    ) {

        // ⭐ Lấy Authorization header từ FE
        String authHeader = httpRequest.getHeader("Authorization");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<ChatbotRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<?> response =
                restTemplate.exchange(
                        CHATBOT_URL,
                        HttpMethod.POST,
                        entity,
                        Object.class
                );

        return ResponseEntity.ok(response.getBody());
    }

}
