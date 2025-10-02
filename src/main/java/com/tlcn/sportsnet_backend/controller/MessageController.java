package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.dto.chat.MessageCreateRequest;
import com.tlcn.sportsnet_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody MessageCreateRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getMessagesByConversationId(@PathVariable String conversationId,  @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(messageService.getMessagesByConversationId(conversationId, page, size));
    }

}
