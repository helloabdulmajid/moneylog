package in.abdulmajid.moneylog.expense.dto.request;

import in.abdulmajid.moneylog.expense.model.Expense;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class ExpenseRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDate expenseDate;

    private LocalTime expenseTime;

    private UUID categoryId;

    private UUID subcategoryId;

    private Expense.PaymentMethod paymentMethod;

    private UUID paymentAppId;

    private UUID paymentAccountId;

    private String notes;

    private String purpose;

    private Boolean isSplit;

    private String splitWith;

    private Boolean isCreditCardBillPayment;

    private UUID linkedExpenseId;
}
