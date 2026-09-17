package in.abdulmajid.moneylog.payment.model;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bill_payments", indexes = {
    @Index(name = "idx_bill_payments_user_date", columnList = "user_id, payment_date"),
    @Index(name = "idx_bill_payments_user_card", columnList = "user_id, credit_card_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillPayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id", nullable = false)
    private CreditCard creditCard;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_time")
    private LocalTime paymentTime;

    @Column(name = "payment_channel")
    private String paymentChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_app_id")
    private PaymentApp paymentApp;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_from_source_id")
    private PaymentSource paidFromSource;

    private String note;
}