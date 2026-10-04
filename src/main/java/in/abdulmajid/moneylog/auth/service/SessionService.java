package in.abdulmajid.moneylog.auth.service;

import in.abdulmajid.moneylog.auth.model.Session;
import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.SessionRepository;
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
 * Trusted, backend-managed session store. All expiry and revocation decisions
 * are enforced here using server-side timestamps — never client-supplied values.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private static final java.time.Duration ACTIVITY_UPDATE_INTERVAL =
            java.time.Duration.ofMinutes(10);

    private final SessionRepository sessionRepository;
    private final TokenService tokenService;

    public enum SessionStatus {
        VALID,
        NOT_FOUND,
        REVOKED,
        OWNER_MISMATCH,
        INACTIVITY_EXPIRED,
        ABSOLUTELY_EXPIRED
    }

    @Transactional
    public Session createSession(User user, long absoluteLifetimeMs, String deviceLabel,
                                 UUID sid, String refreshVerifierHash) {
        LocalDateTime now = LocalDateTime.now();
        Session session = Session.builder()
                .sid(sid)
                .user(user)
                .refreshVerifierHash(refreshVerifierHash)
                .lastActivityAt(now)
                .absoluteExpirationAt(now.plus(absoluteLifetimeMs, ChronoUnit.MILLIS))
                .deviceLabel(deviceLabel)
                .rotationCount(0)
                .build();
        return sessionRepository.save(session);
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

    public SessionStatus checkAccess(UUID sid,
                                     String userEmail,
                                     LocalDateTime now,
                                     long inactivityTimeoutMs) {
        Session session = sessionRepository.findWithUserBySid(sid).orElse(null);
        if (session == null) {
            return SessionStatus.NOT_FOUND;
        }
        if (!userEmail.equals(session.getUser().getEmail())) {
            log.warn("Auth security event: session {} accessed with mismatched user", sid);
            return SessionStatus.OWNER_MISMATCH;
        }
        if (session.getRevokedAt() != null) {
            return SessionStatus.REVOKED;
        }
        if (now.isAfter(session.getAbsoluteExpirationAt())) {
            return SessionStatus.ABSOLUTELY_EXPIRED;
        }
        if (now.isAfter(session.getLastActivityAt().plus(inactivityTimeoutMs, ChronoUnit.MILLIS))) {
            return SessionStatus.INACTIVITY_EXPIRED;
        }
        return SessionStatus.VALID;
    }

    /**
     * Throttled DB-side: only writes when the recorded activity is older than the
     * configured interval, the session is not revoked and not absolutely expired.
     */
    public void touchActivityIfStale(UUID sid, LocalDateTime now) {
        LocalDateTime threshold = now.minus(ACTIVITY_UPDATE_INTERVAL.toMillis(), ChronoUnit.MILLIS);
        sessionRepository.touchActivity(sid, now, threshold);
    }

    /**
     * Revocation must survive the caller rolling back. Reuse detection issues the
     * revoke and then throws; without an independent transaction that revocation
     * would be undone, letting a replayed/reused token keep its session alive.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revoke(UUID sid, LocalDateTime now) {
        sessionRepository.findWithUserBySid(sid).ifPresent(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(now);
                sessionRepository.save(session);
                log.warn("Auth security event: session revoked {}", sid);
            }
        });
    }
}