package in.abdulmajid.moneylog.payment.model;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    private String bankName;

    @Column(name = "last_four_digits")
    private String lastFourDigits;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum AccountType {
        CREDIT_CARD, DEBIT_CARD, BANK_ACCOUNT, WALLET, CASH, OTHER, PAY_LATER
    }
}
