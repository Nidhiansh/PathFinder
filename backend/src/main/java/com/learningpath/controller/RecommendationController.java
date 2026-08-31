package com.learningpath.controller;

import com.learningpath.dto.RecommendationDto;
import com.learningpath.dto.RecommendationFeedbackRequest;
import com.learningpath.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendationDto>> getRecommendations() {
        return ResponseEntity.ok(recommendationService.getRecommendations());
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<Map<String, String>> submitFeedback(
            @PathVariable Long id,
            @RequestBody RecommendationFeedbackRequest request
    ) {
        recommendationService.submitFeedback(id, request);
        return ResponseEntity.ok(Map.of("message", "Feedback recorded successfully"));
    }
}
