package in.abdulmajid.moneylog.admin.controller;

import in.abdulmajid.moneylog.admin.dto.response.AdminUserDetailResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminUserResponse;
import in.abdulmajid.moneylog.admin.service.AdminUserService;
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
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:users:read')")
    public ResponseEntity<Page<AdminUserResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean emailVerified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminUserService.list(search, emailVerified, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:users:read')")
    public ResponseEntity<AdminUserDetailResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.get(id));
    }

    @PostMapping("/{id}/revoke-sessions")
    @PreAuthorize("hasAuthority('admin:users:sessions:revoke')")
    public ResponseEntity<Map<String, String>> revokeSessions(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.revokeSessions(id));
    }

    @PostMapping("/{id}/verify-email")
    @PreAuthorize("hasAuthority('admin:users:verify')")
    public ResponseEntity<Map<String, String>> verifyEmail(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.verifyEmail(id));
    }
}
