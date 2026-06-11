package com.skillexchange.chat;

import com.skillexchange.common.PageResponse;
import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.notification.NotificationType;
import com.skillexchange.session.Session;
import com.skillexchange.session.SessionRepository;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final OnlinePresenceService onlinePresenceService;
    private final SimpMessagingTemplate messagingTemplate;
    @Lazy
    private final NotificationService notificationService;

    @Transactional
    public MessageDTO send(UUID senderId, SendMessageRequest request) {
        log.debug("Sending message sender={} receiver={}", senderId, request.getReceiverId());
        User sender = userRepository.findById(senderId).orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
        Session session = request.getSessionId() == null ? null : sessionRepository.findById(request.getSessionId()).orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        Message saved = messageRepository.save(Message.builder().sender(sender).receiver(receiver).session(session).content(request.getContent()).read(false).build());
        MessageDTO dto = toDto(saved);
        messagingTemplate.convertAndSendToUser(receiver.getId().toString(), "/queue/messages", dto);
        onlinePresenceService.userConnected(senderId);
        if (!onlinePresenceService.isOnline(receiver.getId())) {
            notificationService.createNotification(receiver.getId(), NotificationType.NEW_MESSAGE_RECEIVED, "New message", sender.getFullName() + " sent you a message.");
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public PageResponse<MessageDTO> history(UUID me, UUID other, Pageable pageable) {
        log.debug("Getting message history between {} and {}", me, other);
        return PageResponse.from(messageRepository.history(me, other, pageable).map(this::toDto));
    }

    @Transactional
    public PageResponse<MessageDTO> historyAndMarkRead(UUID me, UUID other, Pageable pageable) {
        messageRepository.markAsRead(other, me);
        return history(me, other, pageable);
    }

    @Transactional(readOnly = true)
    public List<UUID> conversations(UUID userId) {
        log.debug("Listing conversation ids for {}", userId);
        return messageRepository.conversationUserIds(userId);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> conversationSummaries(UUID userId) {
        log.debug("Listing conversation summaries for {}", userId);
        List<ConversationDTO> conversations = new ArrayList<>();
        for (UUID partnerId : messageRepository.findDistinctPartnerIds(userId)) {
            Optional<User> partner = userRepository.findById(partnerId);
            if (partner.isEmpty()) continue;
            List<Message> messages = messageRepository.findTopConversationMessage(userId, partnerId, PageRequest.of(0, 1, Sort.by("sentAt").descending()));
            Optional<Message> last = messages.stream().findFirst();
            User p = partner.get();
            conversations.add(ConversationDTO.builder()
                    .partnerId(p.getId())
                    .partnerEmail(p.getEmail())
                    .partnerName(p.getFullName())
                    .partnerAvatarUrl(p.getProfilePictureUrl())
                    .partnerOnline(onlinePresenceService.isOnline(p.getId()))
                    .lastMessage(last.map(Message::getContent).orElse(null))
                    .lastMessageAt(last.map(Message::getSentAt).orElse(null))
                    .lastMessageIsMe(last.map(m -> m.getSender().getId().equals(userId)).orElse(false))
                    .unreadCount(messageRepository.countUnreadFrom(partnerId, userId))
                    .build());
        }
        conversations.sort(Comparator.comparing((ConversationDTO c) -> c.getLastMessageAt() == null ? LocalDateTime.MIN : c.getLastMessageAt()).reversed());
        return conversations;
    }

    public MessageDTO toDto(Message message) {
        return MessageDTO.builder()
                .id(message.getId()).senderId(message.getSender().getId()).receiverId(message.getReceiver().getId())
                .sessionId(message.getSession() == null ? null : message.getSession().getId())
                .content(message.getContent()).read(message.isRead()).sentAt(message.getSentAt()).build();
    }
}