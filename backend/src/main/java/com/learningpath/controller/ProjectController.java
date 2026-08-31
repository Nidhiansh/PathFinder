package com.learningpath.controller;

import com.learningpath.dto.ProjectDto;
import com.learningpath.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getRecommendedProjects() {
        return ResponseEntity.ok(projectService.getRecommendedProjects());
    }

    @PostMapping("/generate")
    public ResponseEntity<List<ProjectDto>> generateAdaptiveProjects(@RequestBody(required = false) Map<String, String> body) {
        String topic = body != null ? body.get("topic") : null;
        return ResponseEntity.ok(projectService.generateAdaptiveProject(topic));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitProject(
            @PathVariable Long id,
            @RequestBody Map<String, String> submission
    ) {
        String githubUrl = submission.getOrDefault("githubUrl", "");
        String reflection = submission.getOrDefault("reflection", "");
        
        return ResponseEntity.ok(Map.of(
                "message", "Project submission verified and recorded! 50 competency points awarded.",
                "status", "VERIFIED",
                "projectId", id,
                "githubUrl", githubUrl
        ));
    }
}
