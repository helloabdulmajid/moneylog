package in.abdulmajid.moneylog.admin.repository;

import in.abdulmajid.moneylog.admin.model.AdminSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AdminSessionRepository extends JpaRepository<AdminSession, UUID> {

    @Query("SELECT s FROM AdminSession s JOIN FETCH s.admin WHERE s.sid = :sid")
    Optional<AdminSession> findWithAdminBySid(@Param("sid") UUID sid);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE AdminSession s
               SET s.refreshVerifierHash = :newVerifier,
                   s.lastActivityAt = :now,
                   s.rotationCount = s.rotationCount + 1
             WHERE s.sid = :sid
               AND s.refreshVerifierHash = :oldVerifier
               AND s.revokedAt IS NULL
            """)
    int rotateVerifier(@Param("sid") UUID sid,
                       @Param("oldVerifier") String oldVerifier,
                       @Param("newVerifier") String newVerifier,
                       @Param("now") LocalDateTime now);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE AdminSession s
               SET s.lastActivityAt = :now
             WHERE s.sid = :sid
               AND s.lastActivityAt < :threshold
               AND s.revokedAt IS NULL
               AND s.absoluteExpirationAt > :now
            """)
    int touchActivity(@Param("sid") UUID sid,
                      @Param("now") LocalDateTime now,
                      @Param("threshold") LocalDateTime threshold);

    @Transactional
    @Modifying
    @Query("UPDATE AdminSession s SET s.revokedAt = :now WHERE s.admin.id = :adminId AND s.revokedAt IS NULL")
    int revokeByAdminId(@Param("adminId") UUID adminId, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying
    @Query("""
            UPDATE AdminSession s
               SET s.revokedAt = :now
             WHERE s.admin.id = :adminId
               AND s.sid <> :keepSid
               AND s.revokedAt IS NULL
            """)
    int revokeAllExcept(@Param("adminId") UUID adminId,
                        @Param("keepSid") UUID keepSid,
                        @Param("now") LocalDateTime now);

    long countByRevokedAtIsNullAndLastActivityAtAfter(LocalDateTime threshold);
}
