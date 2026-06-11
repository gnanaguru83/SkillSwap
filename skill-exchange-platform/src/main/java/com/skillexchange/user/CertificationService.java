package com.skillexchange.user;

import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.skill.Skill;
import com.skillexchange.skill.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final UserRepository userRepository;
    private final SkillService skillService;

    @Transactional(readOnly = true)
    public List<CertificationDTO> list(UUID userId) {
        log.debug("Listing certifications for user {}", userId);
        return certificationRepository.findByUserIdOrderByYearDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CertificationDTO add(UUID userId, AddCertificationRequest request) {
        log.debug("Adding certification for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Skill skill = request.getSkillId() != null ? skillService.getSkill(request.getSkillId()) : null;
        Certification saved = certificationRepository.save(Certification.builder()
                .user(user)
                .title(request.getTitle())
                .issuer(request.getIssuer())
                .year(request.getYear())
                .credentialUrl(request.getCredentialUrl())
                .skill(skill)
                .build());
        return toDto(saved);
    }

    @Transactional
    public void remove(UUID userId, UUID certificationId) {
        log.debug("Removing certification {} for user {}", certificationId, userId);
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certificationId));
        if (!certification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Certification not found with id: " + certificationId);
        }
        certificationRepository.delete(certification);
    }

    private CertificationDTO toDto(Certification c) {
        return CertificationDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .issuer(c.getIssuer())
                .year(c.getYear())
                .credentialUrl(c.getCredentialUrl())
                .skillId(c.getSkill() != null ? c.getSkill().getId() : null)
                .skillName(c.getSkill() != null ? c.getSkill().getName() : null)
                .build();
    }
}
