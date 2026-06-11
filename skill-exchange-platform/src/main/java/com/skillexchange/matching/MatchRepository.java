package com.skillexchange.matching;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<MatchRequest, UUID> {
    @EntityGraph(attributePaths = {"requester", "target", "teachSkill", "learnSkill"})
    Page<MatchRequest> findByTargetId(UUID targetId, Pageable pageable);

    @EntityGraph(attributePaths = {"requester", "target", "teachSkill", "learnSkill"})
    Page<MatchRequest> findByRequesterId(UUID requesterId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"requester", "target", "teachSkill", "learnSkill"})
    Optional<MatchRequest> findById(UUID id);

    @EntityGraph(attributePaths = {"requester", "target", "teachSkill", "learnSkill"})
    @Query("select m from MatchRequest m where (m.requester.id = :userId or m.target.id = :userId) and m.status in :statuses")
    List<MatchRequest> findUserMatchesWithStatuses(@Param("userId") UUID userId, @Param("statuses") List<MatchStatus> statuses);

    long countByRequesterId(UUID requesterId);
}
