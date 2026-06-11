package com.skillexchange.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlinePresenceService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void userConnected(UUID userId) {
        log.debug("User connected {}", userId);
        redisTemplate.opsForValue().set(key(userId), "1", Duration.ofSeconds(300));
    }

    public void userDisconnected(UUID userId) {
        log.debug("User disconnected {}", userId);
        redisTemplate.delete(key(userId));
    }

    public boolean isOnline(UUID userId) {
        log.debug("Checking online status for {}", userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(userId)));
    }

    public Map<UUID, Boolean> getOnlineUsers(List<UUID> userIds) {
        log.debug("Batch checking online users {}", userIds.size());
        Map<UUID, Boolean> result = new LinkedHashMap<>();
        userIds.forEach(id -> result.put(id, isOnline(id)));
        return result;
    }

    private String key(UUID userId) {
        return "user:online:" + userId;
    }
}
