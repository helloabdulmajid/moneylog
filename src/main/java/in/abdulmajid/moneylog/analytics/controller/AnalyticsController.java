package in.abdulmajid.moneylog.analytics.controller;

import in.abdulmajid.moneylog.analytics.dto.response.MonthlySummary;
import in.abdulmajid.moneylog.analytics.service.AnalyticsService;
import in.abdulmajid.moneylog.common.CurrentUserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserHelper currentUserHelper;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlySummary> getMonthlySummary(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getMonthlySummary(userId, month, year));
    }
}
