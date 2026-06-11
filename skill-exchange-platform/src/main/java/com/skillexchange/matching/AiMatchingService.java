package com.skillexchange.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillexchange.skill.SkillDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiMatchingService {
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    @Value("${openai.api.key:}")
    private String apiKey;
    @Value("${openai.api.url}")
    private String apiUrl;
    @Value("${openai.api.model}")
    private String model;

    public List<MatchSuggestionDTO> enhance(List<MatchSuggestionDTO> matches, List<SkillDTO> learnSkills, List<SkillDTO> teachSkills) {
        log.debug("Enhancing {} matches with AI", matches.size());
        if (matches.isEmpty() || apiKey == null || apiKey.isBlank() || apiKey.startsWith("dummy")) {
            return matches;
        }
        try {
            String prompt = buildPrompt(matches, learnSkills, teachSkills);
            Map<String, Object> request = Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );
            String raw = webClientBuilder.build().post().uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode content = objectMapper.readTree(raw).path("choices").get(0).path("message").path("content");
            Map<UUID, String> reasons = parseReasons(content.asText());
            matches.forEach(match -> match.setAiMatchReason(reasons.get(match.getUserId())));
        } catch (Exception ex) {
            log.error("OpenAI match enhancement failed; returning algorithmic suggestions", ex);
        }
        return matches;
    }

    private String buildPrompt(List<MatchSuggestionDTO> matches, List<SkillDTO> learnSkills, List<SkillDTO> teachSkills) {
        String learn = learnSkills.stream().map(s -> s.getName() + " at " + s.getProficiencyLevel()).toList().toString();
        String teach = teachSkills.stream().map(SkillDTO::getName).toList().toString();
        List<String> partners = new ArrayList<>();
        for (MatchSuggestionDTO match : matches) {
            partners.add(match.getUserId() + " | " + match.getFullName() + " | teaches " + names(match.getTeachSkills()) + " | learns " + names(match.getLearnSkills()) + " | score " + match.getCompatibilityScore());
        }
        return "You are a skill matching expert. A user wants to learn " + learn + ". They can teach " + teach
                + ". Here are 5 potential learning partners: " + partners
                + ". For each partner, write ONE sentence (max 20 words) explaining why they are a good match. Respond in JSON format: [{partnerId: uuid, reason: string}]";
    }

    private String names(List<SkillDTO> skills) {
        return skills.stream().map(SkillDTO::getName).toList().toString();
    }

    private Map<UUID, String> parseReasons(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json.replace("```json", "").replace("```", "").trim());
        java.util.HashMap<UUID, String> reasons = new java.util.HashMap<>();
        for (JsonNode item : node) {
            reasons.put(UUID.fromString(item.path("partnerId").asText()), item.path("reason").asText());
        }
        return reasons;
    }
}
