package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.chat.ConversationResponse;
import com.tlcn.sportsnet_backend.repository.ConversationRepository;
import com.tlcn.sportsnet_backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;
    @GetMapping
    public ResponseEntity<?> findAll() {
        List<ConversationResponse> conversationResponses = conversationService.findAll();
        return ResponseEntity.ok(conversationResponses);
    }
}
