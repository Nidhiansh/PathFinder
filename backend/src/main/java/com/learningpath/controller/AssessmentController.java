package com.learningpath.controller;

import com.learningpath.dto.AssessmentDto;
import com.learningpath.dto.AssessmentResultDto;
import com.learningpath.dto.AssessmentSubmissionRequest;
import com.learningpath.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @GetMapping
    public ResponseEntity<List<AssessmentDto>> getAllAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentDto> getAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<AssessmentResultDto> submitAssessment(
            @PathVariable Long id,
            @RequestBody AssessmentSubmissionRequest request
    ) {
        return ResponseEntity.ok(assessmentService.submitAssessment(id, request));
    }
}
