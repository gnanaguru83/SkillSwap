package com.skillexchange.availability;

import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<Availability> mine(UUID userId) {
        log.debug("Listing availability for user {}", userId);
        return availabilityRepository.findByUserId(userId);
    }

    @Transactional
    public Availability add(UUID userId, AvailabilityRequest request) {
        log.debug("Adding availability for user {}", userId);
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("Availability end time must be after start time");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Availability saved = availabilityRepository.save(Availability.builder()
                .user(user).dayOfWeek(request.getDayOfWeek()).startTime(request.getStartTime())
                .endTime(request.getEndTime()).timezone(request.getTimezone()).build());
        redisTemplate.delete("matches:suggestions:" + userId);
        return saved;
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        log.debug("Deleting availability {} for user {}", id, userId);
        Availability slot = availabilityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));
        if (!slot.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Availability not found with id: " + id);
        }
        availabilityRepository.delete(slot);
        redisTemplate.delete("matches:suggestions:" + userId);
    }

    public int overlapMinutes(Availability a, Availability b) {
        if (a.getDayOfWeek() != b.getDayOfWeek()) return 0;
        java.time.LocalTime start = a.getStartTime().isAfter(b.getStartTime()) ? a.getStartTime() : b.getStartTime();
        java.time.LocalTime end = a.getEndTime().isBefore(b.getEndTime()) ? a.getEndTime() : b.getEndTime();
        return end.isAfter(start) ? (int) Duration.between(start, end).toMinutes() : 0;
    }
}
