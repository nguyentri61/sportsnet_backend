package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.chat.ConversationResponse;
import com.tlcn.sportsnet_backend.dto.chat.MessageCreateRequest;
import com.tlcn.sportsnet_backend.dto.chat.MessageResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.MessageSeenStatus;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ConversationRepository;
import com.tlcn.sportsnet_backend.repository.MessageRepository;
import com.tlcn.sportsnet_backend.repository.MessageStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepository accountRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final FileStorageService fileStorageService;
    @Transactional
    public Object sendMessage(MessageCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        Conversation conversation = conversationRepository.findById(request.getConservationId()).orElseThrow(() -> new InvalidDataException("Conversation not found"));
        if (!conversationRepository.existsByIdAndUser(request.getConservationId(), account.getId())) {
            throw new InvalidDataException("User is not in this conversation");
        }

        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(account);
        message.setConversation(conversation);
        conversation.getMessages().add(message);

        message = messageRepository.save(message);
        List<ConversationParticipant> participants = conversation.getParticipants();
        for(ConversationParticipant participant : participants) {

            MessageStatus messageStatus = MessageStatus.builder()
                    .message(message)
                    .status(participant.getAccount()==account ? MessageSeenStatus.SEEN : MessageSeenStatus.UNSEEN)
                    .account(participant.getAccount())
                            .build();
            messageStatusRepository.save(messageStatus);
            if (!participant.getAccount().getId().equals(account.getId())) {
                Long unreadCountForReceiver = messageStatusRepository.countUnreadMessagesByConversation(participant.getAccount().getId(), conversation.getId());
                // Tạo response update cho người nhận
                ConversationResponse response = ConversationResponse.builder()
                        .id(conversation.getId())
                        .name(account.getUserInfo().getFullName())
                        .avatarUrl(fileStorageService.getFileUrl(account.getUserInfo().getAvatarUrl(), "/avatar"))
                        .firstMessage(message.getContent())
                        .unreadCount(unreadCountForReceiver) // số tin chưa đọc của receiver
                        .build();
                List<MessageStatus> messageStatuses = messageStatusRepository.findByAccountIdAndMessageConversationIdAndStatus(account.getId(), conversation.getId(), MessageSeenStatus.UNSEEN);
                for (MessageStatus mess : messageStatuses) {
                    mess.setStatus(MessageSeenStatus.SEEN);
                    mess.setSeenAt(Instant.now());
                    messageStatusRepository.save(mess);
                }
                // Gửi realtime hội thoại mới/cập nhật
                messagingTemplate.convertAndSend(
                        "/topic/conversation/update/" + participant.getAccount().getId(),
                        response
                );
                messagingTemplate.convertAndSend(
                        "/topic/message/" + conversation.getId() + "/" + participant.getAccount().getId(),
                        toMessageResponse(message, participant.getAccount())
                );
            }
        }
        return "Gửi tin nhắn thành công";
    }

    public PagedResponse<MessageResponse> getMessagesByConversationId(String conversationId,  int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        if (!conversationRepository.existsByIdAndUser(conversationId, account.getId())) {
            throw new InvalidDataException("User is not in this conversation");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findAllByConversationId(conversationId, pageable);
        List<MessageResponse> content = messages.stream()
                .map(message -> toMessageResponse(message, account))
                .toList();
        List<MessageStatus> messageStatuses = messageStatusRepository.findByAccountIdAndMessageConversationIdAndStatus(account.getId(), conversationId, MessageSeenStatus.UNSEEN);
        for (MessageStatus messageStatus : messageStatuses) {
            messageStatus.setStatus(MessageSeenStatus.SEEN);
            messageStatus.setSeenAt(Instant.now());
            messageStatusRepository.save(messageStatus);
        }
        return new PagedResponse<>(
                content,
                messages.getNumber(),
                messages.getSize(),
                messages.getTotalElements(),
                messages.getTotalPages(),
                messages.isLast()
        );
    }

    public MessageResponse toMessageResponse(Message message, Account account) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isReceived(!message.getSender().equals(account))
                .senderName(message.getSender().getUserInfo().getFullName())
                .senderAvatar(fileStorageService.getFileUrl(message.getSender().getUserInfo().getAvatarUrl(), "/avatar"))
                .build();
    }
}
