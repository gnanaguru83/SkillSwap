package com.skillexchange.user;

import com.skillexchange.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Manages a user's structured background: education history and work experience.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackgroundService {
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    private User user(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    // ---- Education ----

    @Transactional(readOnly = true)
    public List<EducationDTO> listEducation(UUID userId) {
        return educationRepository.findByUserIdOrderByEndYearDesc(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public EducationDTO addEducation(UUID userId, EducationDTO request) {
        Education saved = educationRepository.save(Education.builder()
                .user(user(userId))
                .institution(request.getInstitution())
                .degree(request.getDegree())
                .fieldOfStudy(request.getFieldOfStudy())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .build());
        return toDto(saved);
    }

    @Transactional
    public void removeEducation(UUID userId, UUID id) {
        Education e = educationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found with id: " + id));
        if (!e.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Education not found with id: " + id);
        }
        educationRepository.delete(e);
    }

    // ---- Experience ----

    @Transactional(readOnly = true)
    public List<ExperienceDTO> listExperience(UUID userId) {
        return experienceRepository.findByUserIdOrderByEndYearDesc(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ExperienceDTO addExperience(UUID userId, ExperienceDTO request) {
        Experience saved = experienceRepository.save(Experience.builder()
                .user(user(userId))
                .company(request.getCompany())
                .title(request.getTitle())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .description(request.getDescription())
                .build());
        return toDto(saved);
    }

    @Transactional
    public void removeExperience(UUID userId, UUID id) {
        Experience e = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found with id: " + id));
        if (!e.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Experience not found with id: " + id);
        }
        experienceRepository.delete(e);
    }

    private EducationDTO toDto(Education e) {
        return EducationDTO.builder()
                .id(e.getId())
                .institution(e.getInstitution())
                .degree(e.getDegree())
                .fieldOfStudy(e.getFieldOfStudy())
                .startYear(e.getStartYear())
                .endYear(e.getEndYear())
                .build();
    }

    private ExperienceDTO toDto(Experience e) {
        return ExperienceDTO.builder()
                .id(e.getId())
                .company(e.getCompany())
                .title(e.getTitle())
                .startYear(e.getStartYear())
                .endYear(e.getEndYear())
                .description(e.getDescription())
                .build();
    }
}
