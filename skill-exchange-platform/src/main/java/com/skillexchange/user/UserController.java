package com.skillexchange.user;

import com.skillexchange.common.ApiResponse;
import com.skillexchange.common.PageResponse;
import com.skillexchange.rating.BadgeService;
import com.skillexchange.skill.AddSkillRequest;
import com.skillexchange.skill.SkillDTO;
import com.skillexchange.skill.SkillService;
import com.skillexchange.skill.SkillType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {
    private final UserService userService;
    private final SkillService skillService;
    private final BadgeService badgeService;
    private final CertificationService certificationService;
    private final BackgroundService backgroundService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserDTO>> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Current profile", userService.getCurrent(user)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserDTO>> update(@AuthenticationPrincipal User user, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.update(user, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get public user profile")
    public ResponseEntity<ApiResponse<UserDTO>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("User profile", userService.getPublicProfile(id)));
    }

    @GetMapping("/{id}/skills")
    @Operation(summary = "List public user skills")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> userSkills(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("User skills listed", skillService.getUserSkills(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by skill, type, and location")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> search(@RequestParam(required = false) String skill,
                                                                     @RequestParam(required = false) SkillType type,
                                                                     @RequestParam(required = false) String location,
                                                                     Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Users found", userService.search(skill, type, location, pageable)));
    }

    @GetMapping("/me/skills")
    @Operation(summary = "List my skills")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> mySkills(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Skills listed", userService.mySkills(user)));
    }

    @PostMapping("/me/skills")
    @Operation(summary = "Add skill to my profile")
    public ResponseEntity<ApiResponse<SkillDTO>> addSkill(@AuthenticationPrincipal User user, @Valid @RequestBody AddSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill added", skillService.addUserSkill(user.getId(), request)));
    }

    @DeleteMapping("/me/skills/{skillId}")
    @Operation(summary = "Remove skill from my profile")
    public ResponseEntity<ApiResponse<Void>> removeSkill(@AuthenticationPrincipal User user, @PathVariable UUID skillId) {
        skillService.removeUserSkill(user.getId(), skillId);
        return ResponseEntity.ok(ApiResponse.success("Skill removed", null));
    }

    @GetMapping("/me/badges")
    @Operation(summary = "List my badges")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myBadges(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Badges listed", badgeService.badges(user.getId())));
    }

    @GetMapping("/{id}/certifications")
    @Operation(summary = "List a user's courses & certifications")
    public ResponseEntity<ApiResponse<List<CertificationDTO>>> userCertifications(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Certifications listed", certificationService.list(id)));
    }

    @GetMapping("/me/certifications")
    @Operation(summary = "List my courses & certifications")
    public ResponseEntity<ApiResponse<List<CertificationDTO>>> myCertifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Certifications listed", certificationService.list(user.getId())));
    }

    @PostMapping("/me/certifications")
    @Operation(summary = "Add a course or certification to my profile")
    public ResponseEntity<ApiResponse<CertificationDTO>> addCertification(@AuthenticationPrincipal User user, @Valid @RequestBody AddCertificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Certification added", certificationService.add(user.getId(), request)));
    }

    @DeleteMapping("/me/certifications/{certificationId}")
    @Operation(summary = "Remove a course or certification from my profile")
    public ResponseEntity<ApiResponse<Void>> removeCertification(@AuthenticationPrincipal User user, @PathVariable UUID certificationId) {
        certificationService.remove(user.getId(), certificationId);
        return ResponseEntity.ok(ApiResponse.success("Certification removed", null));
    }

    // ---- Education ----

    @GetMapping("/{id}/education")
    @Operation(summary = "List a user's education")
    public ResponseEntity<ApiResponse<List<EducationDTO>>> userEducation(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Education listed", backgroundService.listEducation(id)));
    }

    @GetMapping("/me/education")
    @Operation(summary = "List my education")
    public ResponseEntity<ApiResponse<List<EducationDTO>>> myEducation(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Education listed", backgroundService.listEducation(user.getId())));
    }

    @PostMapping("/me/education")
    @Operation(summary = "Add an education entry")
    public ResponseEntity<ApiResponse<EducationDTO>> addEducation(@AuthenticationPrincipal User user, @Valid @RequestBody EducationDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Education added", backgroundService.addEducation(user.getId(), request)));
    }

    @DeleteMapping("/me/education/{educationId}")
    @Operation(summary = "Remove an education entry")
    public ResponseEntity<ApiResponse<Void>> removeEducation(@AuthenticationPrincipal User user, @PathVariable UUID educationId) {
        backgroundService.removeEducation(user.getId(), educationId);
        return ResponseEntity.ok(ApiResponse.success("Education removed", null));
    }

    // ---- Experience ----

    @GetMapping("/{id}/experience")
    @Operation(summary = "List a user's work experience")
    public ResponseEntity<ApiResponse<List<ExperienceDTO>>> userExperience(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Experience listed", backgroundService.listExperience(id)));
    }

    @GetMapping("/me/experience")
    @Operation(summary = "List my work experience")
    public ResponseEntity<ApiResponse<List<ExperienceDTO>>> myExperience(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Experience listed", backgroundService.listExperience(user.getId())));
    }

    @PostMapping("/me/experience")
    @Operation(summary = "Add a work experience entry")
    public ResponseEntity<ApiResponse<ExperienceDTO>> addExperience(@AuthenticationPrincipal User user, @Valid @RequestBody ExperienceDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Experience added", backgroundService.addExperience(user.getId(), request)));
    }

    @DeleteMapping("/me/experience/{experienceId}")
    @Operation(summary = "Remove a work experience entry")
    public ResponseEntity<ApiResponse<Void>> removeExperience(@AuthenticationPrincipal User user, @PathVariable UUID experienceId) {
        backgroundService.removeExperience(user.getId(), experienceId);
        return ResponseEntity.ok(ApiResponse.success("Experience removed", null));
    }
}