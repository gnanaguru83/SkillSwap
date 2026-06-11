package com.skillexchange.rating;

import com.skillexchange.matching.MatchRepository;
import com.skillexchange.notification.EmailService;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.notification.NotificationType;
import com.skillexchange.session.SessionRepository;
import com.skillexchange.session.SessionStatus;
import com.skillexchange.skill.SkillType;
import com.skillexchange.skill.UserSkillRepository;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final RatingRepository ratingRepository;
    private final MatchRepository matchRepository;
    private final UserSkillRepository userSkillRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public void checkAndAwardBadges(UUID userId) {
        log.debug("Checking badges for user {}", userId);
        User user = userRepository.findById(userId).orElseThrow();
        awardIf(user, "First Session", sessionRepository.countByTeacherIdAndStatus(userId, SessionStatus.COMPLETED) >= 1);
        awardIf(user, "Top Teacher", sessionRepository.countByTeacherIdAndStatus(userId, SessionStatus.COMPLETED) >= 5 && ratingRepository.averageAsTeacher(userId) >= 5.0);
        awardIf(user, "Active Learner", sessionRepository.countByLearnerIdAndStatus(userId, SessionStatus.COMPLETED) >= 10);
        awardIf(user, "Community Builder", matchRepository.countByRequesterId(userId) >= 10);
        awardIf(user, "Skill Master", userSkillRepository.countDistinctSkillByUserIdAndTypeAndActiveTrue(userId, SkillType.TEACH) >= 3 && sessionRepository.countDistinctCompletedSkillsAsTeacher(userId) >= 3);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> badges(UUID userId) {
        log.debug("Listing badges for user {}", userId);
        return userBadgeRepository.findByUserId(userId).stream()
                .map(ub -> {
                    Map<String, Object> badge = new LinkedHashMap<>();
                    badge.put("id", ub.getBadge().getId());
                    badge.put("name", ub.getBadge().getName());
                    badge.put("iconUrl", ub.getBadge().getIconUrl());
                    badge.put("earnedAt", ub.getEarnedAt());
                    return badge;
                })
                .toList();
    }

    private void awardIf(User user, String badgeName, boolean condition) {
        if (!condition) return;
        Badge badge = badgeRepository.findByName(badgeName).orElseThrow();
        if (userBadgeRepository.findByUserIdAndBadgeId(user.getId(), badge.getId()).isPresent()) return;
        userBadgeRepository.save(UserBadge.builder().user(user).badge(badge).build());
        notificationService.createNotification(user.getId(), NotificationType.BADGE_EARNED, "Badge earned", "You earned the " + badgeName + " badge.");
        emailService.sendEmailAsync(user.getEmail(), "You earned a badge", "Congratulations, you earned the " + badgeName + " badge.");
    }
}