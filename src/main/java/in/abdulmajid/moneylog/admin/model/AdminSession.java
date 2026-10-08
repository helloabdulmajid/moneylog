package in.abdulmajid.moneylog.admin.model;

import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "admin_sessions",
        indexes = @Index(name = "idx_admin_session_admin", columnList = "admin_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSession extends BaseEntity {

    @Column(name = "sid", nullable = false, unique = true, updatable = false)
    private UUID sid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

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
