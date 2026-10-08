package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.auth.AdminLoginRateLimiter;
import in.abdulmajid.moneylog.admin.dto.request.AdminChangePasswordRequest;
import in.abdulmajid.moneylog.admin.dto.request.AdminLoginRequest;
import in.abdulmajid.moneylog.admin.dto.response.AdminAuthResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminProfileResponse;
import in.abdulmajid.moneylog.admin.model.Admin;
import in.abdulmajid.moneylog.admin.model.AdminPermission;
import in.abdulmajid.moneylog.admin.repository.AdminRepository;
import in.abdulmajid.moneylog.admin.repository.AdminSessionRepository;
import in.abdulmajid.moneylog.admin.security.AdminContextHelper;
import in.abdulmajid.moneylog.auth.exception.InvalidRefreshTokenException;
import in.abdulmajid.moneylog.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    public static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final AdminRepository adminRepository;
    private final AdminSessionRepository adminSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminSessionService adminSessionService;
    private final AdminLoginRateLimiter loginRateLimiter;
    private final AuditService auditService;
    private final AdminContextHelper adminContextHelper;

    @Value("${admin.session.absolute-lifetime}")
    private long adminSessionAbsoluteLifetimeMs;

    @Value("${admin.session.inactivity-timeout}")
    private long adminSessionInactivityTimeoutMs;

    @Transactional
    public AdminAuthResponse login(AdminLoginRequest request, String clientIp) {
        String email = normalize(request.getEmail());
        loginRateLimiter.check("ip:" + clientIp + "|email:" + email);

        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin == null
                || !Boolean.TRUE.equals(admin.getActive())
                || !passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            log.warn("Admin login failed for {}", email);
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        UUID sid = UUID.randomUUID();
        String accessToken = jwtTokenProvider.generateAdminAccessToken(email, sid.toString());
        String refreshToken = jwtTokenProvider.generateAdminRefreshToken(email, sid.toString());

        adminSessionService.createSession(admin, adminSessionAbsoluteLifetimeMs, "Web", sid,
                adminSessionService.hashVerifier(refreshToken));

        admin.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(admin);
        auditService.record(admin, "admin.login", "admin", admin.getId().toString(), null);

        return buildAuthResponse(admin, accessToken, refreshToken);
    }

    @Transactional
    public AdminAuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)
                || !jwtTokenProvider.isAdminRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String email = jwtTokenProvider.extractEmail(refreshToken);
        String sid = jwtTokenProvider.extractSessionId(refreshToken);
        if (email == null || sid == null) {
            throw new InvalidRefreshTokenException();
        }

        UUID sidUuid;
        try {
            sidUuid = UUID.fromString(sid);
        } catch (IllegalArgumentException e) {
            throw new InvalidRefreshTokenException();
        }

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(InvalidRefreshTokenException::new);
        if (!Boolean.TRUE.equals(admin.getActive())) {
            throw new InvalidRefreshTokenException();
        }

        LocalDateTime now = LocalDateTime.now();
        var session = adminSessionRepository.findWithAdminBySid(sidUuid)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!session.getAdmin().getId().equals(admin.getId())) {
            log.warn("Admin auth security event: refresh token admin/session mismatch {}", sidUuid);
            throw new InvalidRefreshTokenException();
        }
        if (session.getRevokedAt() != null) {
            throw new InvalidRefreshTokenException();
        }
        if (now.isAfter(session.getAbsoluteExpirationAt())) {
            throw new InvalidRefreshTokenException();
        }
        if (now.isAfter(session.getLastActivityAt().plus(
                adminSessionInactivityTimeoutMs, java.time.temporal.ChronoUnit.MILLIS))) {
            throw new InvalidRefreshTokenException();
        }

        String storedVerifier = session.getRefreshVerifierHash();
        if (!adminSessionService.verifierMatches(refreshToken, storedVerifier)) {
            adminSessionService.revoke(sidUuid, now);
            log.warn("Admin auth security event: refresh-token reuse detected, session revoked {}", sidUuid);
            throw new InvalidRefreshTokenException();
        }

        String newAccessToken = jwtTokenProvider.generateAdminAccessToken(email, sid);
        String newRefreshToken = jwtTokenProvider.generateAdminRefreshToken(email, sid);

        int rotated = adminSessionRepository.rotateVerifier(
                sidUuid,
                storedVerifier,
                adminSessionService.hashVerifier(newRefreshToken),
                now);

        if (rotated == 0) {
            adminSessionService.revoke(sidUuid, now);
            log.warn("Admin auth security event: concurrent refresh-token reuse, session revoked {}", sidUuid);
            throw new InvalidRefreshTokenException();
        }

        return buildAuthResponse(admin, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken, String authorizationHeader) {
        String sid = sessionIdFromToken(refreshToken);
        if (sid == null && authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            sid = sessionIdFromToken(authorizationHeader.substring(7));
        }
        if (sid != null) {
            adminSessionService.revoke(UUID.fromString(sid), LocalDateTime.now());
        }
    }

    @Transactional(readOnly = true)
    public AdminProfileResponse me() {
        return toProfile(adminContextHelper.getCurrentAdmin());
    }

    @Transactional
    public Map<String, String> changePassword(AdminChangePasswordRequest request,
                                              String authorizationHeader) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Admin admin = adminContextHelper.getCurrentAdmin();
        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        adminRepository.save(admin);

        String currentSidRaw = sessionIdFromToken(
                authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
                        ? authorizationHeader.substring(7) : null);

        if (currentSidRaw != null) {
            adminSessionRepository.revokeAllExcept(admin.getId(), UUID.fromString(currentSidRaw), LocalDateTime.now());
        } else {
            adminSessionRepository.revokeByAdminId(admin.getId(), LocalDateTime.now());
        }

        auditService.record(admin, "admin.password_changed", "admin", admin.getId().toString(), null);

        return Map.of("message", "Password changed successfully. Other sessions were signed out.");
    }

    public AdminProfileResponse toProfile(Admin admin) {
        List<String> permissions = admin.getRole().permissions().stream()
                .map(AdminPermission::code)
                .sorted()
                .toList();
        return AdminProfileResponse.builder()
                .id(admin.getId())
                .email(admin.getEmail())
                .name(admin.getName())
                .role(admin.getRole().name())
                .permissions(permissions)
                .active(admin.getActive())
                .lastLoginAt(admin.getLastLoginAt())
                .createdAt(admin.getCreatedAt())
                .build();
    }

    private AdminAuthResponse buildAuthResponse(Admin admin, String accessToken, String refreshToken) {
        return AdminAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .admin(toProfile(admin))
                .build();
    }

    private String sessionIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String sid = jwtTokenProvider.extractSessionId(token);
        if (sid == null) {
            return null;
        }
        try {
            UUID.fromString(sid);
            return sid;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
