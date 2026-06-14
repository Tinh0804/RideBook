package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Response.AdminStatsResponse;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Time;
import com.project.BookCarOnline.Service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats/overview")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<AdminStatsResponse> getOverviewStats(@RequestParam(defaultValue = "2026") int year) {
        return APIResponse.<AdminStatsResponse>builder()
                .result(adminService.getOverviewStats(year))
                .build();
    }

}
