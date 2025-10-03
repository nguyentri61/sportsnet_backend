package com.tlcn.sportsnet_backend.controller;


import com.tlcn.sportsnet_backend.dto.chat.TypingEvent;
import com.tlcn.sportsnet_backend.dto.chat.TypingRequest;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Conversation;
import com.tlcn.sportsnet_backend.entity.ConversationParticipant;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ConversationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TypingController {
    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepository accountRepository;
    private final ConversationRepository conversationRepository;

    @MessageMapping("/typing")
    @Transactional
    public void typing(TypingRequest request) {
        Account account = accountRepository.findById(request.getSenderId()).orElse(null);
        Conversation conversation = conversationRepository.findById(request.getConversationId()).orElse(null);

        if (account == null || conversation == null) {
            return; // không tồn tại thì bỏ qua
        }

        List<ConversationParticipant> participants = conversation.getParticipants();
        for (ConversationParticipant participant : participants) {
            if (!participant.getAccount().getId().equals(account.getId())) {
                messagingTemplate.convertAndSend(
                        "/topic/typing/" + request.getConversationId()+"/"+ participant.getAccount().getId(),
                        new TypingEvent(
                                request.getConversationId(),
                                request.getSenderId(),
                                request.isTyping()
                        )
                );
            }
        }
    }

}