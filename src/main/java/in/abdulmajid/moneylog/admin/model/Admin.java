package in.abdulmajid.moneylog.admin.model;

import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends BaseEntity {

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AdminRole role = AdminRole.OWNER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
