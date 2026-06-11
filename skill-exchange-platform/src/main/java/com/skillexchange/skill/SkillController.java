package com.skillexchange.skill;

import com.skillexchange.common.ApiResponse;
import com.skillexchange.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/skills")
@Tag(name = "Skills")
public class SkillController {
    private final SkillService skillService;

    @GetMapping
    @Operation(summary = "List all skills")
    public ResponseEntity<ApiResponse<PageResponse<SkillDTO>>> all(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Skills listed", skillService.all(pageable)));
    }

    @GetMapping("/categories")
    @Operation(summary = "List skill categories")
    public ResponseEntity<ApiResponse<List<String>>> categories() {
        return ResponseEntity.ok(ApiResponse.success("Categories listed", skillService.categories()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new skill")
    public ResponseEntity<ApiResponse<SkillDTO>> create(@Valid @RequestBody AddSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill created", skillService.create(request)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search skills")
    public ResponseEntity<ApiResponse<PageResponse<SkillDTO>>> search(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Skills found", skillService.search(q, pageable)));
    }
}
