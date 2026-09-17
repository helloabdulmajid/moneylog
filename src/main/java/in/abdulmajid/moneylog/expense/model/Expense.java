package in.abdulmajid.moneylog.expense.model;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.category.model.Category;
import in.abdulmajid.moneylog.category.model.Subcategory;
import in.abdulmajid.moneylog.common.BaseEntity;
import in.abdulmajid.moneylog.payment.model.PaymentApp;
import in.abdulmajid.moneylog.payment.model.PaymentMethod;
import in.abdulmajid.moneylog.payment.model.PaymentSource;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_expenses_user_date", columnList = "user_id, expense_date"),
    @Index(name = "idx_expenses_user_category", columnList = "user_id, category_id"),
    @Index(name = "idx_expenses_user_source", columnList = "user_id, payment_source_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "expense_time")
    private LocalTime expenseTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_app_id")
    private PaymentApp paymentApp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_source_id")
    private PaymentSource paymentSource;

    private String notes;

    private String purpose;

    @Column(name = "is_split")
    private Boolean isSplit = false;

    @Column(name = "split_with")
    private String splitWith;
}