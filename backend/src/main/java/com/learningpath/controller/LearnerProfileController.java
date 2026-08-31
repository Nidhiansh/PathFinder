package com.learningpath.controller;

import com.learningpath.dto.ExtractGoalRequest;
import com.learningpath.dto.ExtractGoalResponse;
import com.learningpath.dto.LearnerProfileDto;
import com.learningpath.dto.UpdateProfileRequest;
import com.learningpath.service.LearnerProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class LearnerProfileController {

    @Autowired
    private LearnerProfileService profileService;

    @GetMapping
    public ResponseEntity<LearnerProfileDto> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping
    public ResponseEntity<LearnerProfileDto> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @PostMapping("/extract-goal")
    public ResponseEntity<ExtractGoalResponse> extractGoal(@RequestBody ExtractGoalRequest request) {
        return ResponseEntity.ok(profileService.extractGoalFromPrompt(request));
    }
}
