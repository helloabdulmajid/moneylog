package in.abdulmajid.moneylog.expense.model;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.category.model.Category;
import in.abdulmajid.moneylog.category.model.Subcategory;
import in.abdulmajid.moneylog.common.BaseEntity;
import in.abdulmajid.moneylog.payment.model.PaymentAccount;
import in.abdulmajid.moneylog.payment.model.PaymentApp;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_expenses_user_date", columnList = "user_id, expense_date"),
    @Index(name = "idx_expenses_user_category", columnList = "user_id, category_id"),
    @Index(name = "idx_expenses_user_account", columnList = "user_id, payment_account_id")
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
    @JoinColumn(name = "category_id")
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
    @JoinColumn(name = "payment_account_id")
    private PaymentAccount paymentAccount;

    private String notes;

    private String purpose;

    @Column(name = "is_split")
    private Boolean isSplit = false;

    @Column(name = "split_with")
    private String splitWith;

    @Column(name = "is_credit_card_bill_payment")
    private Boolean isCreditCardBillPayment = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_expense_id")
    private Expense linkedExpense;

    public enum PaymentMethod {
        UPI, CREDIT_CARD, DEBIT_CARD, CASH, BANK_TRANSFER, WALLET, OTHER
    }
}
