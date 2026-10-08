package in.abdulmajid.moneylog.admin.controller;

import in.abdulmajid.moneylog.admin.dto.response.AdminFeedbackResponse;
import in.abdulmajid.moneylog.admin.service.AdminFeedbackService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:feedback:read')")
    public ResponseEntity<Page<AdminFeedbackResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String notificationStatus,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        return ResponseEntity.ok(adminFeedbackService.list(
                status, category, notificationStatus, search, page, size, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:feedback:read')")
    public ResponseEntity<AdminFeedbackResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(adminFeedbackService.get(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin:feedback:write')")
    public ResponseEntity<AdminFeedbackResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminFeedbackService.updateStatus(id, body.get("status")));
    }
}
