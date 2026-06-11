package com.skillexchange.notification;

import com.skillexchange.common.PageResponse;
import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification createNotification(UUID userId, NotificationType type, String title, String message) {
        log.debug("Creating notification type={} user={}", type, userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Notification saved = notificationRepository.save(Notification.builder().user(user).type(type).title(title).message(message).read(false).build());
        redisTemplate.opsForValue().increment("notifications:unread:" + userId);
        redisTemplate.expire("notifications:unread:" + userId, Duration.ofHours(1));
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", saved);
        return saved;
    }

    public PageResponse<Notification> list(UUID userId, Pageable pageable) {
        log.debug("Listing notifications for user {}", userId);
        return PageResponse.from(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable));
    }

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        log.debug("Marking notification read {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.getUser().getId().equals(userId)) throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        notification.setRead(true);
        redisTemplate.delete("notifications:unread:" + userId);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        log.debug("Marking all notifications read for user {}", userId);
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).forEach(n -> n.setRead(true));
        redisTemplate.delete("notifications:unread:" + userId);
    }

    public long unreadCount(UUID userId) {
        log.debug("Counting unread notifications for user {}", userId);
        String key = "notifications:unread:" + userId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Number number) return number.longValue();
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        redisTemplate.opsForValue().set(key, count, Duration.ofHours(1));
        return count;
    }
}
