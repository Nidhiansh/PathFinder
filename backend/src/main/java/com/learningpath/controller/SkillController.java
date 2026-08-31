package com.learningpath.controller;

import com.learningpath.dto.SkillDto;
import com.learningpath.dto.SkillGapDto;
import com.learningpath.dto.UserSkillDto;
import com.learningpath.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping
    public ResponseEntity<List<SkillDto>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/gaps")
    public ResponseEntity<List<SkillGapDto>> getSkillGaps() {
        return ResponseEntity.ok(skillService.calculateSkillGaps());
    }

    @PutMapping("/proficiency")
    public ResponseEntity<UserSkillDto> updateProficiency(@RequestBody Map<String, Object> body) {
        String skillName = (String) body.get("skillName");
        Integer proficiency = Integer.parseInt(body.get("proficiency").toString());
        return ResponseEntity.ok(skillService.updateSkillProficiency(skillName, proficiency));
    }
}
