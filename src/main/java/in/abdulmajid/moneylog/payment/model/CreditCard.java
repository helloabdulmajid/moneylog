package in.abdulmajid.moneylog.payment.model;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_cards", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "issuer", "last_four_digits"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String issuer;

    @Column(name = "last_four_digits")
    private String lastFourDigits;

    @Enumerated(EnumType.STRING)
    private CardNetwork network;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_source_id")
    private PaymentSource paymentSource;
}