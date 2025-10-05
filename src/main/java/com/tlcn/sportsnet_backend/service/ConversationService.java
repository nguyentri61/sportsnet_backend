package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.chat.ConversationResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.ChatRole;
import com.tlcn.sportsnet_backend.enums.ClubMemberRoleEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.ConversationType;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final FileStorageService fileStorageService;
    private final MessageStatusRepository messageStatusRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRepository clubRepository;
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
        conversationResponse.setId(conversation.getId());
        conversationResponse.setFirstMessage(message != null ? message.getContent() : null);
        conversationResponse.setUnreadCount(unRead);
        conversationResponse.setGroupChat(conversation.getType() == ConversationType.GROUP);
        if(conversation.getType() ==  ConversationType.PRIVATE) {
            for (ConversationParticipant conversationParticipant : conversation.getParticipants()) {
                Account accountCheck = conversationParticipant.getAccount();
                if (!conversationParticipant.getAccount().equals(account)) {
                    conversationResponse.setName(accountCheck.getUserInfo().getFullName());
                    conversationResponse.setAvatarUrl(fileStorageService.getFileUrl(accountCheck.getUserInfo().getAvatarUrl(), "/avatar"));
                    break;
                }
            }
        }
        else {
            conversationResponse.setName(conversation.getClub().getName());
            conversationResponse.setAvatarUrl(fileStorageService.getFileUrl(conversation.getClub().getLogoUrl(), "/club/logo"));
        }
       return conversationResponse;
    }

    public void createAllChatConversation() {
        List<Club> clubs = clubRepository.findAll();
        for(Club club : clubs) {
            createConversationByClub(club);
        }

    }

    public void createConversationByClub(Club club) {
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.GROUP);
        conversation.setClub(club);

        // Lưu conversation
        conversation = conversationRepository.save(conversation);

        List<ConversationParticipant> participants = new ArrayList<>();
        List<ClubMember> clubMembers = clubMemberRepository.findByClubIdAndStatus(club.getId(), ClubMemberStatusEnum.APPROVED);
        for(ClubMember clubMember : clubMembers) {
            ConversationParticipant conversationParticipant = new ConversationParticipant();
            conversationParticipant.setAccount(clubMember.getAccount());
            conversationParticipant.setConversation(conversation);
            conversationParticipant.setRole( clubMember.getRole() == ClubMemberRoleEnum.OWNER ? ChatRole.ADMIN : ChatRole.MEMBER);
            participants.add(conversationParticipant);
        }
        conversationParticipantRepository.saveAll(participants);
    }
}
