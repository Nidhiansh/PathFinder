package com.learningpath.controller;

import com.learningpath.dto.LearningPathDto;
import com.learningpath.dto.RecalculateTimeRequest;
import com.learningpath.dto.UpdateItemStatusRequest;
import com.learningpath.service.RoadmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    @Autowired
    private RoadmapService roadmapService;

    @GetMapping({"", "/active"})
    public ResponseEntity<LearningPathDto> getRoadmap() {
        return ResponseEntity.ok(roadmapService.getActiveRoadmap());
    }

    @PostMapping("/generate")
    public ResponseEntity<LearningPathDto> generateRoadmap() {
        return ResponseEntity.ok(roadmapService.generateRoadmapForCurrentUser());
    }

    @PutMapping("/items/{itemId}/status")
    public ResponseEntity<LearningPathDto> updateItemStatus(
            @PathVariable Long itemId,
            @RequestBody UpdateItemStatusRequest request
    ) {
        return ResponseEntity.ok(roadmapService.updateItemStatus(itemId, request));
    }

    @PostMapping("/recalculate-time")
    public ResponseEntity<LearningPathDto> recalculateTime(@RequestBody RecalculateTimeRequest request) {
        return ResponseEntity.ok(roadmapService.recalculateRoadmapTimeline(request));
    }
}
