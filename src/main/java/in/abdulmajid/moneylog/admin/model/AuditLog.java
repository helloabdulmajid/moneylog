package in.abdulmajid.moneylog.admin.model;

import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id", length = 64)
    private String targetId;

    @Column(columnDefinition = "text")
    private String details;

    @Column(length = 64)
    private String ip;
}
