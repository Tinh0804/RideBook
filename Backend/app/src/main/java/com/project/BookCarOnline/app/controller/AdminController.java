package com.project.BookCarOnline.app.controller;

import com.project.BookCarOnline.shared.dto.APIResponse;
import com.project.BookCarOnline.app.dto.reporting.AdminStatsResponse;
import com.project.BookCarOnline.app.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats/overview")
    public APIResponse<AdminStatsResponse> getOverviewStats(
            @RequestParam(required = false, defaultValue = "YEAR") String period,
            @RequestParam(defaultValue = "2026") int year) {
        return APIResponse.<AdminStatsResponse>builder()
                .result(adminService.getOverviewStats(period, year))
                .build();
    }

}
