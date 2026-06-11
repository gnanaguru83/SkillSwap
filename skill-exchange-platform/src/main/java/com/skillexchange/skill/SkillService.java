package com.skillexchange.skill;

import com.skillexchange.common.PageResponse;
import com.skillexchange.exception.DuplicateResourceException;
import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public PageResponse<SkillDTO> all(Pageable pageable) {
        log.debug("Listing skills");
        return PageResponse.from(skillRepository.findAll(pageable).map(this::toDto));
    }

    public PageResponse<SkillDTO> search(String query, Pageable pageable) {
        log.debug("Searching skills {}", query);
        return PageResponse.from(skillRepository.findByNameContainingIgnoreCase(query == null ? "" : query, pageable).map(this::toDto));
    }

    public List<String> categories() {
        log.debug("Listing skill categories");
        return skillRepository.findDistinctCategories();
    }

    @Transactional
    public SkillDTO create(AddSkillRequest request) {
        log.debug("Creating skill {}", request.getName());
        if (request.getName() == null || request.getCategory() == null) {
            throw new ResourceNotFoundException("Skill name and category are required");
        }
        if (skillRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Skill already exists: " + request.getName());
        }
        return toDto(skillRepository.save(Skill.builder().name(request.getName()).category(request.getCategory()).description(request.getSkillDescription()).build()));
    }

    @Transactional
    public SkillDTO addUserSkill(UUID userId, AddSkillRequest request) {
        log.debug("Adding user skill {} for user {}", request.getSkillId(), userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Skill skill = resolveSkill(request);
        userSkillRepository.findByUserIdAndSkillIdAndType(userId, skill.getId(), request.getType()).ifPresent(existing -> {
            throw new DuplicateResourceException("User already has this skill with type " + request.getType());
        });
        UserSkill saved = userSkillRepository.save(UserSkill.builder()
                .user(user).skill(skill).type(request.getType()).proficiencyLevel(request.getProficiencyLevel())
                .description(request.getDescription()).active(true).build());
        redisTemplate.delete("matches:suggestions:" + userId);
        return toDto(saved);
    }

    @Transactional
    public void removeUserSkill(UUID userId, UUID skillId) {
        log.debug("Removing user skill {} for user {}", skillId, userId);
        userSkillRepository.findByUserIdAndSkillIdAndType(userId, skillId, SkillType.TEACH)
                .or(() -> userSkillRepository.findByUserIdAndSkillIdAndType(userId, skillId, SkillType.LEARN))
                .ifPresentOrElse(us -> us.setActive(false), () -> { throw new ResourceNotFoundException("User skill not found"); });
        redisTemplate.delete("matches:suggestions:" + userId);
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> getUserSkills(UUID userId) {
        log.debug("Getting user skills {}", userId);
        return userSkillRepository.findByUserIdAndActiveTrue(userId).stream().map(this::toDto).toList();
    }

    public Skill getSkill(UUID id) {
        log.debug("Getting skill {}", id);
        return skillRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
    }

    private Skill resolveSkill(AddSkillRequest request) {
        if (request.getSkillId() != null) {
            return getSkill(request.getSkillId());
        }
        if (request.getName() == null || request.getCategory() == null) {
            throw new ResourceNotFoundException("Skill id or skill name/category is required");
        }
        return skillRepository.findByNameIgnoreCase(request.getName()).orElseGet(() ->
                skillRepository.save(Skill.builder().name(request.getName()).category(request.getCategory()).description(request.getSkillDescription()).build()));
    }

    public SkillDTO toDto(Skill skill) {
        return SkillDTO.builder().id(skill.getId()).name(skill.getName()).category(skill.getCategory()).description(skill.getDescription()).active(true).build();
    }

    public SkillDTO toDto(UserSkill userSkill) {
        SkillDTO dto = toDto(userSkill.getSkill());
        dto.setType(userSkill.getType());
        dto.setProficiencyLevel(userSkill.getProficiencyLevel());
        dto.setDescription(userSkill.getDescription());
        dto.setActive(userSkill.isActive());
        return dto;
    }
}

