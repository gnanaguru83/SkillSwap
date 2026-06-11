package com.skillexchange.skill;

import com.skillexchange.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    @EntityGraph(attributePaths = {"user", "skill"})
    List<UserSkill> findByUserIdAndActiveTrue(UUID userId);

    @EntityGraph(attributePaths = {"user", "skill"})
    List<UserSkill> findByUserIdAndTypeAndActiveTrue(UUID userId, SkillType type);

    @EntityGraph(attributePaths = {"user", "skill"})
    List<UserSkill> findBySkillIdInAndTypeAndActiveTrue(Collection<UUID> skillIds, SkillType type);

    @EntityGraph(attributePaths = {"user", "skill"})
    Optional<UserSkill> findByUserIdAndSkillIdAndType(UUID userId, UUID skillId, SkillType type);

    @Query("select count(distinct us.skill.id) from UserSkill us where us.user.id = :userId and us.type = :type and us.active = true")
    long countDistinctSkillByUserIdAndTypeAndActiveTrue(@Param("userId") UUID userId, @Param("type") SkillType type);

    @Query("select us from UserSkill us join fetch us.skill where us.user = :user and us.type = :type and us.active = true")
    List<UserSkill> findActiveByUserAndType(@Param("user") User user, @Param("type") SkillType type);
}
