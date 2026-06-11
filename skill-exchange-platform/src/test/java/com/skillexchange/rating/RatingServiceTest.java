package com.skillexchange.rating;

import com.skillexchange.exception.DuplicateResourceException;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.session.Session;
import com.skillexchange.session.SessionRepository;
import com.skillexchange.session.SessionStatus;
import com.skillexchange.skill.Skill;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    @Mock RatingRepository ratingRepository;
    @Mock SessionRepository sessionRepository;
    @Mock UserRepository userRepository;
    @Mock BadgeService badgeService;
    @Mock NotificationService notificationService;
    RatingService service;
    User rater;
    User rated;
    Session session;

    @BeforeEach
    void setup() {
        service = new RatingService(ratingRepository, sessionRepository, userRepository, badgeService, notificationService);
        rater = user("Rater");
        rated = user("Rated");
        Skill skill = Skill.builder().name("Java").category("Engineering").build();
        skill.setId(UUID.randomUUID());
        session = Session.builder().teacher(rated).learner(rater).skill(skill).scheduledAt(LocalDateTime.now().minusDays(1)).durationMinutes(60).status(SessionStatus.COMPLETED).build();
        session.setId(UUID.randomUUID());
    }

    @Test
    void testSubmitRating_success() {
        SubmitRatingRequest request = request(5);
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(ratingRepository.findBySessionIdAndRaterId(session.getId(), rater.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(rater.getId())).thenReturn(Optional.of(rater));
        when(userRepository.findById(rated.getId())).thenReturn(Optional.of(rated));
        when(ratingRepository.save(any())).thenAnswer(inv -> {
            Rating rating = inv.getArgument(0);
            rating.setId(UUID.randomUUID());
            return rating;
        });
        RatingDTO dto = service.submit(rater.getId(), request);
        assertThat(dto.getScore()).isEqualTo(5);
    }

    @Test
    void testSubmitRating_sessionNotCompleted() {
        session.setStatus(SessionStatus.SCHEDULED);
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        assertThatThrownBy(() -> service.submit(rater.getId(), request(4))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSubmitRating_alreadyRated() {
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(ratingRepository.findBySessionIdAndRaterId(session.getId(), rater.getId())).thenReturn(Optional.of(Rating.builder().build()));
        assertThatThrownBy(() -> service.submit(rater.getId(), request(4))).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void testBadgeAwardedAfterFirstSession() {
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(ratingRepository.findBySessionIdAndRaterId(session.getId(), rater.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(rater.getId())).thenReturn(Optional.of(rater));
        when(userRepository.findById(rated.getId())).thenReturn(Optional.of(rated));
        when(ratingRepository.save(any())).thenAnswer(inv -> {
            Rating rating = inv.getArgument(0);
            rating.setId(UUID.randomUUID());
            return rating;
        });
        service.submit(rater.getId(), request(5));
        verify(badgeService).checkAndAwardBadges(rated.getId());
    }

    private SubmitRatingRequest request(int score) {
        SubmitRatingRequest request = new SubmitRatingRequest();
        request.setSessionId(session.getId());
        request.setRatedUserId(rated.getId());
        request.setScore(score);
        request.setFeedback("Great session");
        return request;
    }

    private User user(String name) {
        User user = User.builder().email(name.toLowerCase() + "@example.com").fullName(name).passwordHash("hash").build();
        user.setId(UUID.randomUUID());
        return user;
    }
}
