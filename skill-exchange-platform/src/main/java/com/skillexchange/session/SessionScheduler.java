package com.skillexchange.session;

import com.skillexchange.notification.EmailService;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionScheduler {
    private final SessionRepository sessionRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendSessionReminders() {
        log.debug("Running session reminder job");
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.findByStatusAndScheduledAtBetweenAndReminderSentFalse(SessionStatus.SCHEDULED, now, now.plusHours(2)).forEach(session -> {
            session.setReminderSent(true);
            notificationService.createNotification(session.getTeacher().getId(), NotificationType.SESSION_REMINDER, "Session reminder", "Your session starts within two hours.");
            notificationService.createNotification(session.getLearner().getId(), NotificationType.SESSION_REMINDER, "Session reminder", "Your session starts within two hours.");
            emailService.sendEmailAsync(session.getTeacher().getEmail(), "Session reminder", "Your skill exchange session starts soon.");
            emailService.sendEmailAsync(session.getLearner().getEmail(), "Session reminder", "Your skill exchange session starts soon.");
        });
    }

    @Scheduled(fixedDelay = 1_800_000)
    @Transactional
    public void markNoShowSessions() {
        log.debug("Running no-show job");
        sessionRepository.findByStatusAndScheduledAtBefore(SessionStatus.SCHEDULED, LocalDateTime.now().minusMinutes(30))
                .forEach(session -> session.setStatus(SessionStatus.NO_SHOW));
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkSessionCompletion() {
        log.debug("Running rating prompt job");
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.findByStatusAndScheduledAtBefore(SessionStatus.SCHEDULED, now).stream()
                .filter(session -> !session.isRatingPromptSent())
                .filter(session -> session.getScheduledAt().plusMinutes(session.getDurationMinutes()).isBefore(now))
                .forEach(session -> {
                    session.setRatingPromptSent(true);
                    notificationService.createNotification(session.getTeacher().getId(), NotificationType.RATE_SESSION, "How was your session?", "Please rate your completed session.");
                    notificationService.createNotification(session.getLearner().getId(), NotificationType.RATE_SESSION, "How was your session?", "Please rate your completed session.");
                });
    }
}
