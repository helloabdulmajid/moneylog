package in.abdulmajid.moneylog.auth.model;

import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "auth_sessions",
        indexes = @Index(name = "idx_auth_session_user", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session extends BaseEntity {

    @Column(name = "sid", nullable = false, unique = true, updatable = false)
    private UUID sid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_verifier_hash", nullable = false)
    private String refreshVerifierHash;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "absolute_expiration_at", nullable = false)
    private LocalDateTime absoluteExpirationAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "device_label")
    private String deviceLabel;

    @Column(name = "rotation_count", nullable = false)
    @Builder.Default
    private Integer rotationCount = 0;
}