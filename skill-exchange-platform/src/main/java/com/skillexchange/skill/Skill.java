package com.skillexchange.skill;

import com.skillexchange.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "skills")
public class Skill extends AuditableEntity {
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String category;
    private String description;
}
