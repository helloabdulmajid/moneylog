package in.abdulmajid.moneylog.expense.dto.response;

import in.abdulmajid.moneylog.category.dto.response.CategoryResponse;
import in.abdulmajid.moneylog.category.dto.response.SubcategoryResponse;
import in.abdulmajid.moneylog.expense.model.Expense;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAccountResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class ExpenseResponse {
    private UUID id;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private LocalTime expenseTime;
    private CategoryResponse category;
    private SubcategoryResponse subcategory;
    private Expense.PaymentMethod paymentMethod;
    private PaymentAppResponse paymentApp;
    private PaymentAccountResponse paymentAccount;
    private String notes;
    private String purpose;
    private Boolean isSplit;
    private String splitWith;
    private Boolean isCreditCardBillPayment;
    private UUID linkedExpenseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
