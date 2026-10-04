package in.abdulmajid.moneylog.auth.repository;

import in.abdulmajid.moneylog.auth.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Query("SELECT s FROM Session s JOIN FETCH s.user WHERE s.sid = :sid")
    Optional<Session> findWithUserBySid(@Param("sid") UUID sid);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Session s
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
            UPDATE Session s
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
    @Query("DELETE FROM Session s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}