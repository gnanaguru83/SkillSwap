package com.skillexchange.session;

import com.skillexchange.matching.MatchRepository;
import com.skillexchange.matching.MatchRequest;
import com.skillexchange.matching.MatchStatus;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.rating.BadgeService;
import com.skillexchange.skill.Skill;
import com.skillexchange.skill.SkillService;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    @Mock SessionRepository sessionRepository;
    @Mock MatchRepository matchRepository;
    @Mock UserRepository userRepository;
    @Mock SkillService skillService;
    @Mock NotificationService notificationService;
    @Mock BadgeService badgeService;
    SessionService service;
    User teacher;
    User learner;
    Skill skill;
    MatchRequest match;

    @BeforeEach
    void setup() {
        service = new SessionService(sessionRepository, matchRepository, userRepository, skillService, notificationService, badgeService);
        teacher = user("Teacher");
        learner = user("Learner");
        skill = Skill.builder().name("Java").category("Engineering").build();
        skill.setId(UUID.randomUUID());
        match = MatchRequest.builder().requester(learner).target(teacher).teachSkill(skill).learnSkill(skill).status(MatchStatus.ACCEPTED).build();
        match.setId(UUID.randomUUID());
    }

    @Test
    void testBookSession_success() {
        BookSessionRequest request = request(LocalDateTime.now().plusDays(1));
        when(matchRepository.findById(request.getMatchId())).thenReturn(Optional.of(match));
        when(sessionRepository.findPotentialConflicts(anyList(), any())).thenReturn(List.of());
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(userRepository.findById(learner.getId())).thenReturn(Optional.of(learner));
        when(skillService.getSkill(skill.getId())).thenReturn(skill);
        when(sessionRepository.save(any())).thenAnswer(inv -> {
            Session s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        SessionDTO dto = service.book(teacher.getId(), request);
        assertThat(dto.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
    }

    @Test
    void testBookSession_sessionConflict() {
        BookSessionRequest request = request(LocalDateTime.now().plusDays(1));
        Session existing = session(request.getScheduledAt().plusMinutes(15), 60);
        when(matchRepository.findById(request.getMatchId())).thenReturn(Optional.of(match));
        when(sessionRepository.findPotentialConflicts(anyList(), any())).thenReturn(List.of(existing));
        assertThatThrownBy(() -> service.book(teacher.getId(), request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCancelSession_byTeacher_success() {
        Session session = session(LocalDateTime.now().plusDays(1), 60);
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        SessionDTO dto = service.cancel(teacher.getId(), session.getId());
        assertThat(dto.getStatus()).isEqualTo(SessionStatus.CANCELLED);
    }

    @Test
    void testCancelSession_tooLate() {
        Session session = session(LocalDateTime.now().plusMinutes(30), 60);
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        assertThatThrownBy(() -> service.cancel(teacher.getId(), session.getId())).isInstanceOf(IllegalArgumentException.class);
    }

    private BookSessionRequest request(LocalDateTime when) {
        BookSessionRequest request = new BookSessionRequest();
        request.setMatchId(match.getId());
        request.setTeacherId(teacher.getId());
        request.setLearnerId(learner.getId());
        request.setSkillId(skill.getId());
        request.setScheduledAt(when);
        request.setDurationMinutes(60);
        return request;
    }

    private Session session(LocalDateTime when, int minutes) {
        Session session = Session.builder().match(match).teacher(teacher).learner(learner).skill(skill).scheduledAt(when).durationMinutes(minutes).status(SessionStatus.SCHEDULED).build();
        session.setId(UUID.randomUUID());
        return session;
    }

    private User user(String name) {
        User user = User.builder().email(name.toLowerCase() + "@example.com").fullName(name).passwordHash("hash").build();
        user.setId(UUID.randomUUID());
        return user;
    }
}
