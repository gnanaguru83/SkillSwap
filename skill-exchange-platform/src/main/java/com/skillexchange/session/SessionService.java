package com.skillexchange.session;

import com.skillexchange.common.PageResponse;
import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.matching.MatchRepository;
import com.skillexchange.matching.MatchRequest;
import com.skillexchange.matching.MatchStatus;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.notification.NotificationType;
import com.skillexchange.rating.BadgeService;
import com.skillexchange.skill.Skill;
import com.skillexchange.skill.SkillService;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final SkillService skillService;
    private final NotificationService notificationService;
    @Lazy
    private final BadgeService badgeService;

    @Transactional
    public SessionDTO book(UUID currentUserId, BookSessionRequest request) {
        log.debug("Booking session for match {}", request.getMatchId());
        MatchRequest match = matchRepository.findById(request.getMatchId()).orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + request.getMatchId()));
        if (match.getStatus() != MatchStatus.ACCEPTED) throw new IllegalArgumentException("Only accepted matches can be scheduled");
        if (!currentUserId.equals(request.getTeacherId()) && !currentUserId.equals(request.getLearnerId())) throw new IllegalArgumentException("Current user must be part of the session");
        boolean teacherInMatch = match.getRequester().getId().equals(request.getTeacherId()) || match.getTarget().getId().equals(request.getTeacherId());
        boolean learnerInMatch = match.getRequester().getId().equals(request.getLearnerId()) || match.getTarget().getId().equals(request.getLearnerId());
        if (!teacherInMatch || !learnerInMatch || request.getTeacherId().equals(request.getLearnerId())) throw new IllegalArgumentException("Session participants must be the accepted match participants");
        LocalDateTime end = request.getScheduledAt().plusMinutes(request.getDurationMinutes());
        boolean conflict = sessionRepository.findPotentialConflicts(List.of(request.getTeacherId(), request.getLearnerId()), end).stream()
                .anyMatch(existing -> existing.getScheduledAt().plusMinutes(existing.getDurationMinutes()).isAfter(request.getScheduledAt()));
        if (conflict) {
            throw new IllegalArgumentException("Session conflicts with an existing booking");
        }
        User teacher = userRepository.findById(request.getTeacherId()).orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        User learner = userRepository.findById(request.getLearnerId()).orElseThrow(() -> new ResourceNotFoundException("Learner not found"));
        Skill skill = skillService.getSkill(request.getSkillId());
        Session saved = sessionRepository.save(Session.builder().match(match).teacher(teacher).learner(learner).skill(skill)
                .scheduledAt(request.getScheduledAt()).durationMinutes(request.getDurationMinutes()).meetingLink(request.getMeetingLink())
                .notes(request.getNotes()).status(SessionStatus.SCHEDULED).build());
        notificationService.createNotification(teacher.getId(), NotificationType.SESSION_BOOKED, "Session booked", "A session was booked for " + skill.getName());
        notificationService.createNotification(learner.getId(), NotificationType.SESSION_BOOKED, "Session booked", "A session was booked for " + skill.getName());
        return toDto(saved);
    }

    public PageResponse<SessionDTO> upcoming(UUID userId, Pageable pageable) {
        log.debug("Listing upcoming sessions for {}", userId);
        return PageResponse.from(sessionRepository.upcoming(userId, LocalDateTime.now(), pageable).map(this::toDto));
    }

    public PageResponse<SessionDTO> history(UUID userId, Pageable pageable) {
        log.debug("Listing session history for {}", userId);
        return PageResponse.from(sessionRepository.history(userId, LocalDateTime.now(), pageable).map(this::toDto));
    }

    public SessionDTO get(UUID userId, UUID id) {
        log.debug("Getting session {}", id);
        Session session = owned(userId, id);
        return toDto(session);
    }

    @Transactional
    public SessionDTO cancel(UUID userId, UUID id) {
        log.debug("Cancelling session {}", id);
        Session session = owned(userId, id);
        if (session.getScheduledAt().minusHours(1).isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Sessions cannot be cancelled within one hour of start time");
        session.setStatus(SessionStatus.CANCELLED);
        notificationService.createNotification(session.getTeacher().getId(), NotificationType.SESSION_CANCELLED, "Session cancelled", "A scheduled session was cancelled.");
        notificationService.createNotification(session.getLearner().getId(), NotificationType.SESSION_CANCELLED, "Session cancelled", "A scheduled session was cancelled.");
        return toDto(session);
    }

    @Transactional
    public SessionDTO complete(UUID userId, UUID id) {
        log.debug("Completing session {}", id);
        Session session = owned(userId, id);
        session.setStatus(SessionStatus.COMPLETED);
        badgeService.checkAndAwardBadges(session.getTeacher().getId());
        badgeService.checkAndAwardBadges(session.getLearner().getId());
        notificationService.createNotification(session.getTeacher().getId(), NotificationType.RATE_SESSION, "Rate your session", "Please rate your recent session.");
        notificationService.createNotification(session.getLearner().getId(), NotificationType.RATE_SESSION, "Rate your session", "Please rate your recent session.");
        return toDto(session);
    }

    private Session owned(UUID userId, UUID id) {
        Session session = sessionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        if (!session.getTeacher().getId().equals(userId) && !session.getLearner().getId().equals(userId)) throw new ResourceNotFoundException("Session not found with id: " + id);
        return session;
    }

    public SessionDTO toDto(Session session) {
        return SessionDTO.builder().id(session.getId()).matchId(session.getMatch().getId())
                .teacherId(session.getTeacher().getId()).teacherName(session.getTeacher().getFullName())
                .learnerId(session.getLearner().getId()).learnerName(session.getLearner().getFullName())
                .skillId(session.getSkill().getId()).skillName(session.getSkill().getName())
                .scheduledAt(session.getScheduledAt()).durationMinutes(session.getDurationMinutes())
                .meetingLink(session.getMeetingLink()).status(session.getStatus()).notes(session.getNotes()).build();
    }
}
