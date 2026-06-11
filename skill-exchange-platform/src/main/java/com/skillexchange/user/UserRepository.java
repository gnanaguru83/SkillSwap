package com.skillexchange.user;

import com.skillexchange.skill.SkillType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  /**
   * Search users by free-text skill term, skill type, and location.
   *
   * Semantics (each filter independent and optional):
   * - skill (text): matches the user's full name, email, OR the name of any of
   *   their active skills (case-insensitive substring). When null, not applied.
   * - type (TEACH/LEARN): user must have at least one active skill of that type.
   *   Applied independently of the text term, so the Discover "Teach"/"Learn"
   *   toggle works on its own. When null, not applied.
   * - location: case-insensitive substring match (e.g. "chenn" matches
   *   "Chennai"). When null, not applied.
   * - When no filter is supplied, ALL active users are returned (including users
   *   with zero skills).
   *
   * NOTE: every string parameter is wrapped in cast(... as string). Without the
   * cast, Hibernate binds a null/untyped parameter as bytea and PostgreSQL fails
   * with "function lower(bytea) does not exist", which previously made every
   * search (and the matching fallback that calls it) return HTTP 500.
   */
  @Query("""
      select distinct u from User u
      where u.active = true
        and (
          :skill is null
          or lower(u.fullName) like lower(concat('%', cast(:skill as string), '%'))
          or lower(u.email) like lower(concat('%', cast(:skill as string), '%'))
          or exists (
            select 1 from UserSkill us
            join us.skill s
            where us.user = u
              and us.active = true
              and lower(s.name) like lower(concat('%', cast(:skill as string), '%'))
          )
        )
        and (
          :type is null
          or exists (
            select 1 from UserSkill ust
            where ust.user = u
              and ust.active = true
              and ust.type = :type
          )
        )
        and (
          :location is null
          or lower(u.location) like lower(concat('%', cast(:location as string), '%'))
        )
      """)
  Page<User> search(
      @Param("skill") String skill,
      @Param("type") SkillType type,
      @Param("location") String location,
      Pageable pageable);
}
