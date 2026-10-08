package in.abdulmajid.moneylog.admin.controller;

import in.abdulmajid.moneylog.admin.dto.request.AdminChangePasswordRequest;
import in.abdulmajid.moneylog.admin.dto.request.AdminLoginRequest;
import in.abdulmajid.moneylog.admin.dto.request.AdminRefreshRequest;
import in.abdulmajid.moneylog.admin.dto.response.AdminAuthResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminProfileResponse;
import in.abdulmajid.moneylog.admin.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Hidden
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                                   HttpServletRequest servletRequest) {
        return ResponseEntity.ok(adminAuthService.login(request, clientIp(servletRequest)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AdminAuthResponse> refresh(@Valid @RequestBody AdminRefreshRequest request) {
        return ResponseEntity.ok(adminAuthService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody AdminRefreshRequest request,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        adminAuthService.logout(request.getRefreshToken(), authorization);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> me() {
        return ResponseEntity.ok(adminAuthService.me());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody AdminChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(adminAuthService.changePassword(request, authorization));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
