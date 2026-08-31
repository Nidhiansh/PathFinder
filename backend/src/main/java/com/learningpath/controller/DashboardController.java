package com.learningpath.controller;

import com.learningpath.dto.DashboardSummaryDto;
import com.learningpath.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardSummaryDto> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}
