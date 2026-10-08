package in.abdulmajid.moneylog.admin.controller;

import in.abdulmajid.moneylog.admin.dto.response.AdminDashboardResponse;
import in.abdulmajid.moneylog.admin.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:dashboard:view')")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminDashboardService.dashboard());
    }
}
