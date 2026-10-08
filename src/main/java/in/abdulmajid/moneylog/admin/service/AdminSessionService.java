package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.model.Admin;
import in.abdulmajid.moneylog.admin.model.AdminSession;
import in.abdulmajid.moneylog.admin.repository.AdminSessionRepository;
import in.abdulmajid.moneylog.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Backend-managed admin session store. Same trust model as user sessions:
 * server-side timestamps only, verifier rotation with reuse detection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSessionService {

    private static final java.time.Duration ACTIVITY_UPDATE_INTERVAL =
            java.time.Duration.ofMinutes(10);

    private final AdminSessionRepository adminSessionRepository;
    private final TokenService tokenService;

    public enum AdminSessionStatus {
        VALID,
        NOT_FOUND,
        REVOKED,
        OWNER_MISMATCH,
        INACTIVITY_EXPIRED,
        ABSOLUTELY_EXPIRED
    }

    @Transactional
    public AdminSession createSession(Admin admin, long absoluteLifetimeMs, String deviceLabel,
                                      UUID sid, String refreshVerifierHash) {
        LocalDateTime now = LocalDateTime.now();
        AdminSession session = AdminSession.builder()
                .sid(sid)
                .admin(admin)
                .refreshVerifierHash(refreshVerifierHash)
                .lastActivityAt(now)
                .absoluteExpirationAt(now.plus(absoluteLifetimeMs, ChronoUnit.MILLIS))
                .deviceLabel(deviceLabel)
                .rotationCount(0)
                .build();
        return adminSessionRepository.save(session);
    }

    public String hashVerifier(String refreshToken) {
        return tokenService.hash(refreshToken);
    }

    public boolean verifierMatches(String rawToken, String storedHash) {
        if (rawToken == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        try {
            byte[] expected = HexFormat.of().parseHex(storedHash);
            byte[] actual = HexFormat.of().parseHex(tokenService.hash(rawToken));
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public AdminSessionStatus checkAccess(UUID sid,
                                          String adminEmail,
                                          LocalDateTime now,
                                          long inactivityTimeoutMs) {
        AdminSession session = adminSessionRepository.findWithAdminBySid(sid).orElse(null);
        if (session == null) {
            return AdminSessionStatus.NOT_FOUND;
        }
        if (!adminEmail.equals(session.getAdmin().getEmail())) {
            log.warn("Admin auth security event: session {} accessed with mismatched admin", sid);
            return AdminSessionStatus.OWNER_MISMATCH;
        }
        if (session.getRevokedAt() != null) {
            return AdminSessionStatus.REVOKED;
        }
        if (now.isAfter(session.getAbsoluteExpirationAt())) {
            return AdminSessionStatus.ABSOLUTELY_EXPIRED;
        }
        if (now.isAfter(session.getLastActivityAt().plus(inactivityTimeoutMs, ChronoUnit.MILLIS))) {
            return AdminSessionStatus.INACTIVITY_EXPIRED;
        }
        return AdminSessionStatus.VALID;
    }

    public void touchActivityIfStale(UUID sid, LocalDateTime now) {
        LocalDateTime threshold = now.minus(ACTIVITY_UPDATE_INTERVAL.toMillis(), ChronoUnit.MILLIS);
        adminSessionRepository.touchActivity(sid, now, threshold);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revoke(UUID sid, LocalDateTime now) {
        adminSessionRepository.findWithAdminBySid(sid).ifPresent(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(now);
                adminSessionRepository.save(session);
                log.warn("Admin auth security event: session revoked {}", sid);
            }
        });
    }
}
