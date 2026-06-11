package com.skillexchange.matching;

import com.skillexchange.availability.AvailabilityRepository;
import com.skillexchange.availability.AvailabilityService;
import com.skillexchange.chat.OnlinePresenceService;
import com.skillexchange.notification.NotificationService;
import com.skillexchange.rating.RatingRepository;
import com.skillexchange.skill.ProficiencyLevel;
import com.skillexchange.skill.Skill;
import com.skillexchange.skill.SkillDTO;
import com.skillexchange.skill.SkillService;
import com.skillexchange.skill.SkillType;
import com.skillexchange.skill.UserSkill;
import com.skillexchange.skill.UserSkillRepository;
import com.skillexchange.user.User;
import com.skillexchange.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {
    @Mock UserRepository userRepository;
    @Mock UserSkillRepository userSkillRepository;
    @Mock AvailabilityRepository availabilityRepository;
    @Mock AvailabilityService availabilityService;
    @Mock MatchRepository matchRepository;
    @Mock SkillService skillService;
    @Mock RatingRepository ratingRepository;
    @Mock AiMatchingService aiMatchingService;
    @Mock OnlinePresenceService onlinePresenceService;
    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOperations;
    @Mock NotificationService notificationService;
    MatchingService service;
    User userA;
    Skill python;
    Skill java;

    @BeforeEach
    void setup() {
        service = new MatchingService(userRepository, userSkillRepository, availabilityRepository, availabilityService, matchRepository, skillService, ratingRepository, aiMatchingService, onlinePresenceService, redisTemplate, notificationService);
        userA = user("User A", "Austin");
        python = skill("Python");
        java = skill("Java");
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(any())).thenReturn(null);
        lenient().when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        lenient().when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(userA.getId(), SkillType.LEARN)).thenReturn(List.of(userSkill(userA, java, SkillType.LEARN, ProficiencyLevel.BEGINNER)));
        lenient().when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(userA.getId(), SkillType.TEACH)).thenReturn(List.of(userSkill(userA, python, SkillType.TEACH, ProficiencyLevel.EXPERT)));
        lenient().when(availabilityRepository.findByUserId(any())).thenReturn(List.of());
        lenient().when(matchRepository.findUserMatchesWithStatuses(eq(userA.getId()), anyList())).thenReturn(List.of());
        lenient().when(ratingRepository.averageForUser(any())).thenReturn(0.0);
        lenient().when(onlinePresenceService.getOnlineUsers(anyList())).thenReturn(Map.of());
        lenient().when(aiMatchingService.enhance(anyList(), anyList(), anyList())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(skillService.toDto(any(UserSkill.class))).thenAnswer(inv -> SkillDTO.builder().id(((UserSkill) inv.getArgument(0)).getSkill().getId()).name(((UserSkill) inv.getArgument(0)).getSkill().getName()).build());
    }

    @Test
    void testFindMatches_mutualSwapScoresHigher() {
        User userB = user("User B", "Austin");
        when(userSkillRepository.findBySkillIdInAndTypeAndActiveTrue(any(), eq(SkillType.TEACH))).thenReturn(List.of(userSkill(userB, java, SkillType.TEACH, ProficiencyLevel.ADVANCED)));
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(userB.getId(), SkillType.LEARN)).thenReturn(List.of(userSkill(userB, python, SkillType.LEARN, ProficiencyLevel.BEGINNER)));
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(userB.getId(), SkillType.TEACH)).thenReturn(List.of(userSkill(userB, java, SkillType.TEACH, ProficiencyLevel.ADVANCED)));
        List<MatchSuggestionDTO> matches = service.findMatches(userA.getId());
        assertThat(matches).first().extracting(MatchSuggestionDTO::getUserId).isEqualTo(userB.getId());
        assertThat(matches.get(0).getCompatibilityScore()).isGreaterThanOrEqualTo(80);
    }

    @Test
    void testFindMatches_noMutualSwap_lowerScore() {
        User mutual = user("Mutual", "Austin");
        User oneWay = user("One Way", "Austin");
        when(userSkillRepository.findBySkillIdInAndTypeAndActiveTrue(any(), eq(SkillType.TEACH))).thenReturn(List.of(userSkill(mutual, java, SkillType.TEACH, ProficiencyLevel.ADVANCED), userSkill(oneWay, java, SkillType.TEACH, ProficiencyLevel.ADVANCED)));
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(mutual.getId(), SkillType.LEARN)).thenReturn(List.of(userSkill(mutual, python, SkillType.LEARN, ProficiencyLevel.BEGINNER)));
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(mutual.getId(), SkillType.TEACH)).thenReturn(List.of(userSkill(mutual, java, SkillType.TEACH, ProficiencyLevel.ADVANCED)));
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(oneWay.getId(), SkillType.LEARN)).thenReturn(List.of());
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(oneWay.getId(), SkillType.TEACH)).thenReturn(List.of(userSkill(oneWay, java, SkillType.TEACH, ProficiencyLevel.ADVANCED)));
        List<MatchSuggestionDTO> matches = service.findMatches(userA.getId());
        assertThat(matches.get(0).getCompatibilityScore()).isGreaterThan(matches.get(1).getCompatibilityScore());
    }

    @Test
    void testFindMatches_excludesAlreadyMatchedUsers() {
        User userD = user("User D", "Austin");
        MatchRequest accepted = MatchRequest.builder().requester(userA).target(userD).status(MatchStatus.ACCEPTED).build();
        when(matchRepository.findUserMatchesWithStatuses(eq(userA.getId()), anyList())).thenReturn(List.of(accepted));
        when(userSkillRepository.findBySkillIdInAndTypeAndActiveTrue(any(), eq(SkillType.TEACH))).thenReturn(List.of(userSkill(userD, java, SkillType.TEACH, ProficiencyLevel.EXPERT)));
        assertThat(service.findMatches(userA.getId())).isEmpty();
    }

    @Test
    void testFindMatches_locationBonusAdded() {
        User same = user("Same", "Austin");
        User different = user("Different", "Boston");
        when(userSkillRepository.findBySkillIdInAndTypeAndActiveTrue(any(), eq(SkillType.TEACH))).thenReturn(List.of(userSkill(same, java, SkillType.TEACH, ProficiencyLevel.INTERMEDIATE), userSkill(different, java, SkillType.TEACH, ProficiencyLevel.INTERMEDIATE)));
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(same.getId(), SkillType.LEARN)).thenReturn(List.of());
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(same.getId(), SkillType.TEACH)).thenReturn(List.of(userSkill(same, java, SkillType.TEACH, ProficiencyLevel.INTERMEDIATE)));
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(different.getId(), SkillType.LEARN)).thenReturn(List.of());
        when(userSkillRepository.findByUserIdAndTypeAndActiveTrue(different.getId(), SkillType.TEACH)).thenReturn(List.of(userSkill(different, java, SkillType.TEACH, ProficiencyLevel.INTERMEDIATE)));
        List<MatchSuggestionDTO> matches = service.findMatches(userA.getId());
        assertThat(matches.get(0).getCompatibilityScore() - matches.get(1).getCompatibilityScore()).isEqualTo(10);
    }

    private User user(String name, String location) {
        User user = User.builder().email(name.replace(" ", "").toLowerCase() + "@example.com").fullName(name).passwordHash("hash").location(location).build();
        user.setId(UUID.randomUUID());
        return user;
    }

    private Skill skill(String name) {
        Skill skill = Skill.builder().name(name).category("Engineering").build();
        skill.setId(UUID.randomUUID());
        return skill;
    }

    private UserSkill userSkill(User user, Skill skill, SkillType type, ProficiencyLevel level) {
        UserSkill userSkill = UserSkill.builder().user(user).skill(skill).type(type).proficiencyLevel(level).active(true).build();
        userSkill.setId(UUID.randomUUID());
        return userSkill;
    }
}
