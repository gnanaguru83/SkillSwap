package com.skillexchange.session;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    @EntityGraph(attributePaths = {"match", "teacher", "learner", "skill"})
    @Query("select s from Session s where (s.teacher.id = :userId or s.learner.id = :userId) and s.status = com.skillexchange.session.SessionStatus.SCHEDULED and s.scheduledAt >= :now order by s.scheduledAt")
    Page<Session> upcoming(@Param("userId") UUID userId, @Param("now") LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"match", "teacher", "learner", "skill"})
    @Query("select s from Session s where (s.teacher.id = :userId or s.learner.id = :userId) and (s.status <> com.skillexchange.session.SessionStatus.SCHEDULED or s.scheduledAt < :now) order by s.scheduledAt desc")
    Page<Session> history(@Param("userId") UUID userId, @Param("now") LocalDateTime now, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"match", "teacher", "learner", "skill"})
    Optional<Session> findById(UUID id);

    @Query("select s from Session s where s.status = com.skillexchange.session.SessionStatus.SCHEDULED and (s.teacher.id in :userIds or s.learner.id in :userIds) and s.scheduledAt < :end")
    List<Session> findPotentialConflicts(@Param("userIds") List<UUID> userIds, @Param("end") LocalDateTime end);

    List<Session> findByStatusAndScheduledAtBetweenAndReminderSentFalse(SessionStatus status, LocalDateTime start, LocalDateTime end);
    List<Session> findByStatusAndScheduledAtBefore(SessionStatus status, LocalDateTime cutoff);
    long countByTeacherIdAndStatus(UUID teacherId, SessionStatus status);
    long countByLearnerIdAndStatus(UUID learnerId, SessionStatus status);
    @Query("select count(distinct s.skill.id) from Session s where s.teacher.id = :teacherId and s.status = com.skillexchange.session.SessionStatus.COMPLETED")
    long countDistinctCompletedSkillsAsTeacher(@Param("teacherId") UUID teacherId);
}