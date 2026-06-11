package com.skillexchange.user;

import com.skillexchange.common.PageResponse;
import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.rating.RatingRepository;
import com.skillexchange.session.SessionRepository;
import com.skillexchange.session.SessionStatus;
import com.skillexchange.skill.SkillDTO;
import com.skillexchange.skill.SkillService;
import com.skillexchange.skill.SkillType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SkillService skillService;
    private final RatingRepository ratingRepository;
    private final SessionRepository sessionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by email {}", username);
        return userRepository.findByEmailIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User findEntity(UUID id) {
        log.debug("Finding user entity {}", id);
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserDTO getCurrent(User current) {
        log.debug("Getting current user {}", current.getId());
        return enrich(findEntity(current.getId()));
    }

    public UserDTO getPublicProfile(UUID id) {
        log.debug("Getting public profile {}", id);
        return enrich(findEntity(id));
    }

    @Transactional
    public UserDTO update(User current, UpdateProfileRequest request) {
        log.debug("Updating profile {}", current.getId());
        User user = findEntity(current.getId());
        if (request.getFullName() != null && !request.getFullName().isBlank()) user.setFullName(request.getFullName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getProfilePictureUrl() != null) user.setProfilePictureUrl(request.getProfilePictureUrl());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getLinkedinUrl() != null) user.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getHeadline() != null) user.setHeadline(request.getHeadline());
        if (request.getLanguages() != null) user.setLanguages(request.getLanguages());
        return enrich(user);
    }

    public PageResponse<UserDTO> search(String skill, SkillType type, String location, Pageable pageable) {
        log.debug("Searching users skill={} type={} location={}", skill, type, location);
        String normalizedSkill = skill == null || skill.isBlank() ? null : skill.trim();
        String normalizedLocation = location == null || location.isBlank() ? null : location.trim();
        return PageResponse.from(userRepository.search(normalizedSkill, type, normalizedLocation, pageable).map(this::enrich));
    }

    /**
     * Maps a User to a UserDTO and attaches aggregate reputation stats
     * (average rating, rating count, total completed sessions) so the Discover
     * list and profile page can surface them consistently.
     */
    private UserDTO enrich(User user) {
        UserDTO dto = userMapper.toDto(user);
        UUID id = user.getId();
        double avg = ratingRepository.averageForUser(id);
        dto.setAverageRating(Math.round(avg * 10.0) / 10.0);
        dto.setRatingCount(ratingRepository.countByRatedUserId(id));
        long sessions = sessionRepository.countByTeacherIdAndStatus(id, SessionStatus.COMPLETED)
                + sessionRepository.countByLearnerIdAndStatus(id, SessionStatus.COMPLETED);
        dto.setTotalSessions(sessions);
        return dto;
    }

    public List<SkillDTO> mySkills(User current) {
        log.debug("Listing skills for user {}", current.getId());
        return skillService.getUserSkills(current.getId());
    }
}
