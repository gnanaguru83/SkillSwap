package com.skillexchange.skill;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    Page<Skill> findByNameContainingIgnoreCase(String query, Pageable pageable);
    @Query("select distinct s.category from Skill s order by s.category")
    List<String> findDistinctCategories();
}
