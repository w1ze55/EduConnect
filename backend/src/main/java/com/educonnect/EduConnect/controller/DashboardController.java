package com.educonnect.EduConnect.controller;

import com.educonnect.EduConnect.dto.DashboardResponseDTO;
import com.educonnect.EduConnect.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardResponseDTO> getResumo(
            @RequestParam(required = false) Long escolaId,
            @RequestParam(required = false) Long turmaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String datePreset,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(dashboardService.getResumo(escolaId, turmaId, usuarioId, datePreset, startDate, endDate));
    }
}
