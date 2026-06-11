package com.skillexchange.rating;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {
    Optional<Rating> findBySessionIdAndRaterId(UUID sessionId, UUID raterId);
    Page<Rating> findByRatedUserId(UUID ratedUserId, Pageable pageable);
    long countByRatedUserId(UUID ratedUserId);
    @Query("select coalesce(avg(r.score), 0) from Rating r where r.ratedUser.id = :userId")
    double averageForUser(@Param("userId") UUID userId);
    @Query("select coalesce(avg(r.score), 0) from Rating r where r.ratedUser.id = :userId and r.session.teacher.id = :userId")
    double averageAsTeacher(@Param("userId") UUID userId);
    @Query("select count(r) from Rating r where r.ratedUser.id = :userId and r.session.teacher.id = :userId and r.score = 5")
    long fiveStarTeacherRatings(@Param("userId") UUID userId);
}
