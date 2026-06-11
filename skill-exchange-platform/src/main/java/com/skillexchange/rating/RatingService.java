package com.skillexchange.rating;

import com.skillexchange.common.PageResponse;
import com.skillexchange.exception.DuplicateResourceException;
import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.notification.NotificationType;
import com.skillexchange.session.Session;
import com.skillexchange.session.SessionRepository;
import com.skillexchange.session.SessionStatus;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BadgeService badgeService;
    private final NotificationService notificationService;

    @Transactional
    public RatingDTO submit(UUID raterId, SubmitRatingRequest request) {
        log.debug("Submitting rating session={} rater={}", request.getSessionId(), raterId);
        Session session = sessionRepository.findById(request.getSessionId()).orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + request.getSessionId()));
        if (session.getStatus() != SessionStatus.COMPLETED) throw new IllegalArgumentException("Ratings can only be submitted for completed sessions");
        if (!session.getTeacher().getId().equals(raterId) && !session.getLearner().getId().equals(raterId)) throw new IllegalArgumentException("Rater must belong to the session");
        if (request.getRatedUserId().equals(raterId)) throw new IllegalArgumentException("Users cannot rate themselves");
        if (!session.getTeacher().getId().equals(request.getRatedUserId()) && !session.getLearner().getId().equals(request.getRatedUserId())) throw new IllegalArgumentException("Rated user must belong to the session");
        if (ratingRepository.findBySessionIdAndRaterId(session.getId(), raterId).isPresent()) throw new DuplicateResourceException("Session already rated by this user");
        User rater = userRepository.findById(raterId).orElseThrow(() -> new ResourceNotFoundException("Rater not found"));
        User rated = userRepository.findById(request.getRatedUserId()).orElseThrow(() -> new ResourceNotFoundException("Rated user not found"));
        Rating saved = ratingRepository.save(Rating.builder().session(session).rater(rater).ratedUser(rated).score(request.getScore()).feedback(request.getFeedback()).build());
        notificationService.createNotification(rated.getId(), NotificationType.NEW_RATING_RECEIVED, "New rating received", rater.getFullName() + " rated your session.");
        badgeService.checkAndAwardBadges(rater.getId());
        badgeService.checkAndAwardBadges(rated.getId());
        return toDto(saved);
    }

    public PageResponse<RatingDTO> userRatings(UUID userId, Pageable pageable) {
        log.debug("Listing ratings for user {}", userId);
        return PageResponse.from(ratingRepository.findByRatedUserId(userId, pageable).map(this::toDto));
    }

    public RatingDTO toDto(Rating rating) {
        return RatingDTO.builder().id(rating.getId()).sessionId(rating.getSession().getId())
                .raterId(rating.getRater().getId()).raterName(rating.getRater().getFullName())
                .ratedUserId(rating.getRatedUser().getId()).ratedUserName(rating.getRatedUser().getFullName())
                .score(rating.getScore()).feedback(rating.getFeedback()).createdAt(rating.getCreatedAt()).build();
    }
}
