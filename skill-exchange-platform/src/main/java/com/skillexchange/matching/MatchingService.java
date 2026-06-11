package com.skillexchange.matching;

import com.skillexchange.availability.Availability;
import com.skillexchange.availability.AvailabilityRepository;
import com.skillexchange.availability.AvailabilityService;
import com.skillexchange.chat.OnlinePresenceService;
import com.skillexchange.common.PageResponse;
import com.skillexchange.exception.ResourceNotFoundException;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.notification.NotificationType;
import com.skillexchange.rating.RatingRepository;
import com.skillexchange.skill.Skill;
import com.skillexchange.skill.SkillDTO;
import com.skillexchange.skill.SkillService;
import com.skillexchange.skill.SkillType;
import com.skillexchange.skill.UserSkill;
import com.skillexchange.skill.UserSkillRepository;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

        private final UserRepository userRepository;
        private final UserSkillRepository userSkillRepository;
        private final AvailabilityRepository availabilityRepository;
        private final AvailabilityService availabilityService;
        private final MatchRepository matchRepository;
        private final SkillService skillService;
        private final RatingRepository ratingRepository;
        private final AiMatchingService aiMatchingService;
        private final OnlinePresenceService onlinePresenceService;
        private final RedisTemplate<String, Object> redisTemplate;
        @Lazy
        private final NotificationService notificationService;

        @Transactional(readOnly = true)
        @SuppressWarnings("unchecked")
        public List<MatchSuggestionDTO> findMatches(UUID userId) {
                log.debug("Finding matches for user {}", userId);
                String key = "matches:suggestions:" + userId;
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached instanceof List<?> list && !list.isEmpty()
                                && list.get(0) instanceof MatchSuggestionDTO) {
                        return (List<MatchSuggestionDTO>) cached;
                }

                User me = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with id: " + userId));

                List<UserSkill> myLearn = userSkillRepository
                                .findByUserIdAndTypeAndActiveTrue(userId, SkillType.LEARN);
                List<UserSkill> myTeach = userSkillRepository
                                .findByUserIdAndTypeAndActiveTrue(userId, SkillType.TEACH);

                Set<UUID> learnSkillIds = myLearn.stream()
                                .map(us -> us.getSkill().getId())
                                .collect(Collectors.toSet());

                Set<UUID> excluded = excludedUserIds(userId);
                excluded.add(userId); // never suggest yourself

                // --- Candidate collection ---
                // Path A: skill-matched candidates (current user has LEARN skills)
                Set<UUID> candidateIds = new HashSet<>();
                Map<UUID, List<UserSkill>> byCandidate;

                if (!learnSkillIds.isEmpty()) {
                        List<UserSkill> candidateTeachSkills = userSkillRepository
                                        .findBySkillIdInAndTypeAndActiveTrue(learnSkillIds, SkillType.TEACH)
                                        .stream()
                                        .filter(us -> !excluded.contains(us.getUser().getId()))
                                        .toList();

                        byCandidate = candidateTeachSkills.stream()
                                        .collect(Collectors.groupingBy(us -> us.getUser().getId()));
                        candidateIds.addAll(byCandidate.keySet());
                } else {
                        byCandidate = new java.util.HashMap<>();
                }

                // Path B: fallback — include ALL other active users not yet in candidateIds and
                // not excluded
                // This ensures users with no skills (newly registered) still appear.
                // We fetch up to 50 extra users to keep it bounded.
                List<User> fallbackUsers = userRepository
                                .search(null, null, null, PageRequest.of(0, 50))
                                .getContent()
                                .stream()
                                .filter(u -> !u.getId().equals(userId)
                                                && !excluded.contains(u.getId())
                                                && !candidateIds.contains(u.getId()))
                                .toList();

                List<Availability> myAvailability = availabilityRepository.findByUserId(userId);

                Set<UUID> allCandidateIds = new HashSet<>(candidateIds);
                fallbackUsers.forEach(u -> allCandidateIds.add(u.getId()));

                Map<UUID, Boolean> online = onlinePresenceService.getOnlineUsers(new ArrayList<>(allCandidateIds));

                List<MatchSuggestionDTO> suggestions = new ArrayList<>();

                // Score skill-matched candidates
                for (Map.Entry<UUID, List<UserSkill>> entry : byCandidate.entrySet()) {
                        User candidate = entry.getValue().get(0).getUser();
                        List<UserSkill> candidateLearn = userSkillRepository
                                        .findByUserIdAndTypeAndActiveTrue(candidate.getId(), SkillType.LEARN);
                        List<UserSkill> candidateTeach = userSkillRepository
                                        .findByUserIdAndTypeAndActiveTrue(candidate.getId(), SkillType.TEACH);

                        Score score = score(me, myTeach, myAvailability,
                                        candidate, candidateLearn, candidateTeach);

                        // Lower threshold: skill overlap already established so show all
                        suggestions.add(buildSuggestion(candidate, candidateTeach, candidateLearn,
                                        score, online));
                }

                // Score fallback candidates (no skill overlap — lower base score)
                for (User candidate : fallbackUsers) {
                        List<UserSkill> candidateLearn = userSkillRepository
                                        .findByUserIdAndTypeAndActiveTrue(candidate.getId(), SkillType.LEARN);
                        List<UserSkill> candidateTeach = userSkillRepository
                                        .findByUserIdAndTypeAndActiveTrue(candidate.getId(), SkillType.TEACH);

                        Score score = score(me, myTeach, myAvailability,
                                        candidate, candidateLearn, candidateTeach);

                        // Only include fallback candidates who have a genuine skill
                        // relationship (score > 0 means they want to learn what I teach).
                        if (score.value() > 0) {
                                suggestions.add(buildSuggestion(candidate, candidateTeach, candidateLearn,
                                                score, online));
                        }
                }

                List<MatchSuggestionDTO> top = suggestions.stream()
                                .sorted(Comparator.comparingInt(
                                                MatchSuggestionDTO::getCompatibilityScore).reversed())
                                .limit(20)
                                .toList();

                List<SkillDTO> learnDtos = myLearn.stream()
                                .map(skillService::toDto).toList();
                List<SkillDTO> teachDtos = myTeach.stream()
                                .map(skillService::toDto).toList();

                // Only pass top-5 to AI if we have something to enhance
                List<MatchSuggestionDTO> toEnhance = top.stream().limit(5).toList();
                List<MatchSuggestionDTO> enhanced = toEnhance.isEmpty()
                                ? List.of()
                                : aiMatchingService.enhance(toEnhance, learnDtos, teachDtos);

                List<MatchSuggestionDTO> result = new ArrayList<>(top);
                Map<UUID, MatchSuggestionDTO> enhancedById = enhanced.stream()
                                .collect(Collectors.toMap(
                                                MatchSuggestionDTO::getUserId,
                                                Function.identity()));

                result.forEach(match -> {
                        MatchSuggestionDTO enriched = enhancedById.get(match.getUserId());
                        if (enriched != null) {
                                match.setAiMatchReason(enriched.getAiMatchReason());
                        }
                });

                if (!result.isEmpty()) {
                        redisTemplate.opsForValue().set(key, result, Duration.ofMinutes(30));
                }
                return result;
        }

        @Transactional
        public MatchRequestDTO request(UUID requesterId, UUID targetId,
                        UUID teachSkillId, UUID learnSkillId, String message) {
                log.debug("Creating match request requester={} target={}",
                                requesterId, targetId);

                User requester = userRepository.findById(requesterId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Requester not found"));
                User target = userRepository.findById(targetId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Target user not found"));

                Skill teach = skillService.getSkill(teachSkillId);
                Skill learn = skillService.getSkill(learnSkillId);

                int score = findMatches(requesterId).stream()
                                .filter(m -> m.getUserId().equals(targetId))
                                .findFirst()
                                .map(MatchSuggestionDTO::getCompatibilityScore)
                                .orElse(50);

                MatchRequest saved = matchRepository.save(MatchRequest.builder()
                                .requester(requester)
                                .target(target)
                                .teachSkill(teach)
                                .learnSkill(learn)
                                .message(message)
                                .compatibilityScore(score)
                                .build());

                notificationService.createNotification(
                                targetId,
                                NotificationType.MATCH_REQUEST_RECEIVED,
                                "New match request",
                                requester.getFullName() + " wants to learn with you.");

                return toDto(saved);
        }

        @Transactional(readOnly = true)
        public PageResponse<MatchRequestDTO> received(UUID userId, Pageable pageable) {
                log.debug("Listing received match requests {}", userId);
                return PageResponse.from(
                                matchRepository.findByTargetId(userId, pageable).map(this::toDto));
        }

        @Transactional(readOnly = true)
        public PageResponse<MatchRequestDTO> sent(UUID userId, Pageable pageable) {
                log.debug("Listing sent match requests {}", userId);
                return PageResponse.from(
                                matchRepository.findByRequesterId(userId, pageable).map(this::toDto));
        }

        @Transactional
        public MatchRequestDTO accept(UUID userId, UUID id) {
                log.debug("Accepting match {}", id);
                MatchRequest match = ownedTarget(userId, id);
                match.setStatus(MatchStatus.ACCEPTED);
                notificationService.createNotification(
                                match.getRequester().getId(),
                                NotificationType.MATCH_ACCEPTED,
                                "Match accepted",
                                match.getTarget().getFullName() + " accepted your request.");
                return toDto(match);
        }

        @Transactional
        public MatchRequestDTO reject(UUID userId, UUID id) {
                log.debug("Rejecting match {}", id);
                MatchRequest match = ownedTarget(userId, id);
                match.setStatus(MatchStatus.REJECTED);
                notificationService.createNotification(
                                match.getRequester().getId(),
                                NotificationType.MATCH_REJECTED,
                                "Match rejected",
                                match.getTarget().getFullName() + " rejected your request.");
                return toDto(match);
        }

        // ---- helpers ----

        private MatchSuggestionDTO buildSuggestion(User candidate,
                        List<UserSkill> candidateTeach, List<UserSkill> candidateLearn,
                        Score score, Map<UUID, Boolean> online) {
                return MatchSuggestionDTO.builder()
                                .userId(candidate.getId())
                                .fullName(candidate.getFullName())
                                .profilePicture(candidate.getProfilePictureUrl())
                                .teachSkills(candidateTeach.stream().map(skillService::toDto).toList())
                                .learnSkills(candidateLearn.stream().map(skillService::toDto).toList())
                                .compatibilityScore(score.value())
                                .averageRating(score.averageRating())
                                .commonAvailability(score.commonAvailability())
                                .location(candidate.getLocation())
                                .online(online.getOrDefault(candidate.getId(), false))
                                .build();
        }

        private MatchRequest ownedTarget(UUID userId, UUID id) {
                MatchRequest match = matchRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Match request not found with id: " + id));
                if (!match.getTarget().getId().equals(userId)) {
                        throw new ResourceNotFoundException(
                                        "Match request not found with id: " + id);
                }
                return match;
        }

        private Set<UUID> excludedUserIds(UUID userId) {
                Set<UUID> ids = new HashSet<>();
                matchRepository.findUserMatchesWithStatuses(userId,
                                List.of(MatchStatus.ACCEPTED,
                                                MatchStatus.REJECTED,
                                                MatchStatus.PENDING))
                                .forEach(match -> ids.add(
                                                match.getRequester().getId().equals(userId)
                                                                ? match.getTarget().getId()
                                                                : match.getRequester().getId()));
                return ids;
        }

        private Score score(User me, List<UserSkill> myTeach,
                        List<Availability> myAvailability, User candidate,
                        List<UserSkill> candidateLearn, List<UserSkill> candidateTeach) {

                Set<UUID> myTeachIds = myTeach.stream()
                                .map(us -> us.getSkill().getId())
                                .collect(Collectors.toSet());

                Set<UUID> myLearnIds = userSkillRepository
                                .findByUserIdAndTypeAndActiveTrue(me.getId(), SkillType.LEARN)
                                .stream()
                                .map(us -> us.getSkill().getId())
                                .collect(Collectors.toSet());

                // Skills the candidate can teach that I actually want to learn
                List<UserSkill> matchedTeach = candidateTeach.stream()
                                .filter(us -> myLearnIds.contains(us.getSkill().getId()))
                                .toList();
                boolean teachesWhatIWant = !matchedTeach.isEmpty();
                // Candidate wants to learn something I can teach (mutual swap)
                boolean wantsWhatITeach = !myTeachIds.isEmpty() && candidateLearn.stream()
                                .anyMatch(us -> myTeachIds.contains(us.getSkill().getId()));

                double avg = ratingRepository.averageForUser(candidate.getId());
                List<String> overlaps = new ArrayList<>();
                boolean sameLocation = me.getLocation() != null && candidate.getLocation() != null
                                && me.getLocation().equalsIgnoreCase(candidate.getLocation());

                // No teach/learn overlap in either direction: this is only a
                // "discovery" suggestion for browsing, NOT a real skill match. Use a
                // flat base of 20 so a profile with no matching skills always shows a
                // low 20% match. Real skill matches start at 40+ (see below).
                if (!teachesWhatIWant && !wantsWhatITeach) {
                        return new Score(20, avg, overlaps);
                }

                int score = 0;
                if (teachesWhatIWant)
                        score += 50; // candidate teaches a skill I want to learn
                if (wantsWhatITeach)
                        score += 40; // mutual: I teach a skill they want to learn

                // Bonus for depth: advanced/expert proficiency on the matched teaching
                // skills (capped so it stays secondary to the core skill match).
                int proficiencyBonus = (int) matchedTeach.stream()
                                .filter(us -> us.getProficiencyLevel().name().equals("ADVANCED")
                                                || us.getProficiencyLevel().name().equals("EXPERT"))
                                .count() * 5;
                score += Math.min(proficiencyBonus, 15);

                // Availability overlap (secondary, capped)
                int availabilityBonus = 0;
                List<Availability> candidateAvailability = availabilityRepository.findByUserId(candidate.getId());
                for (Availability mine : myAvailability) {
                        for (Availability theirs : candidateAvailability) {
                                int minutes = availabilityService.overlapMinutes(mine, theirs);
                                if (minutes >= 30) {
                                        availabilityBonus += 2;
                                        overlaps.add(mine.getDayOfWeek() + " "
                                                        + mine.getStartTime() + "-" + mine.getEndTime());
                                }
                        }
                }
                score += Math.min(availabilityBonus, 10);

                // Same location (secondary)
                if (sameLocation) {
                        score += 5;
                }

                // High rating (secondary)
                if (avg >= 4.5)
                        score += 5;

                return new Score(Math.min(score, 100), avg, overlaps);
        }

        public MatchRequestDTO toDto(MatchRequest match) {
                return MatchRequestDTO.builder()
                                .id(match.getId())
                                .requesterId(match.getRequester().getId())
                                .requesterName(match.getRequester().getFullName())
                                .targetId(match.getTarget().getId())
                                .targetName(match.getTarget().getFullName())
                                .teachSkillId(match.getTeachSkill().getId())
                                .teachSkillName(match.getTeachSkill().getName())
                                .learnSkillId(match.getLearnSkill().getId())
                                .learnSkillName(match.getLearnSkill().getName())
                                .status(match.getStatus())
                                .message(match.getMessage())
                                .compatibilityScore(match.getCompatibilityScore())
                                .createdAt(match.getCreatedAt())
                                .build();
        }

        private record Score(int value, double averageRating,
                        List<String> commonAvailability) {
        }
}
