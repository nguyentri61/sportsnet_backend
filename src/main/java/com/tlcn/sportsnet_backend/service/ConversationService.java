package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.chat.ConversationResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Conversation;
import com.tlcn.sportsnet_backend.entity.ConversationParticipant;
import com.tlcn.sportsnet_backend.entity.Message;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ConversationRepository;
import com.tlcn.sportsnet_backend.repository.MessageRepository;
import com.tlcn.sportsnet_backend.repository.MessageStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final FileStorageService fileStorageService;
    private final MessageStatusRepository messageStatusRepository;
    public List<ConversationResponse> findAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        List<Conversation> conversations = conversationRepository.findAllByUserOrderByUpdated(account.getId());
        return  conversations.stream()
                .map(event -> toConversationResponse(event, account))
                .toList();

    }

    public ConversationResponse toConversationResponse(Conversation conversation, Account account) {
        Long unRead = messageStatusRepository.countUnreadMessagesByConversation(account.getId(), conversation.getId());
        ConversationResponse conversationResponse = new ConversationResponse();
        Message message = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId()).orElse(null);
        for(ConversationParticipant conversationParticipant : conversation.getParticipants()) {
            Account accountCheck = conversationParticipant.getAccount();
            if(!conversationParticipant.getAccount().equals(account)) {
                conversationResponse.setId(conversation.getId());
                conversationResponse.setName(accountCheck.getUserInfo().getFullName());
                conversationResponse.setAvatarUrl(fileStorageService.getFileUrl(accountCheck.getUserInfo().getAvatarUrl(), "/avatar"));
                conversationResponse.setFirstMessage(message != null ? message.getContent() : null);
                conversationResponse.setUnreadCount(unRead);
                break;
            }
        }
       return conversationResponse;
    }
}
