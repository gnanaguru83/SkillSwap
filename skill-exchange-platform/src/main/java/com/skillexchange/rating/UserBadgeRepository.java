package com.skillexchange.rating;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {
    Optional<UserBadge> findByUserIdAndBadgeId(UUID userId, UUID badgeId);

    @EntityGraph(attributePaths = {"badge"})
    List<UserBadge> findByUserId(UUID userId);
}