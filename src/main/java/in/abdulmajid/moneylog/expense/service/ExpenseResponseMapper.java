package in.abdulmajid.moneylog.expense.service;

import in.abdulmajid.moneylog.category.dto.response.CategoryResponse;
import in.abdulmajid.moneylog.category.dto.response.SubcategoryResponse;
import in.abdulmajid.moneylog.expense.dto.response.ExpenseResponse;
import in.abdulmajid.moneylog.expense.model.Expense;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentSourceResponse;

public final class ExpenseResponseMapper {

    private ExpenseResponseMapper() {
    }

    public static ExpenseResponse map(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .expenseTime(expense.getExpenseTime())
                .category(expense.getCategory() != null ?
                    CategoryResponse.builder()
                        .id(expense.getCategory().getId())
                        .name(expense.getCategory().getName())
                        .icon(expense.getCategory().getIcon())
                        .color(expense.getCategory().getColor())
                        .build() : null)
                .subcategory(expense.getSubcategory() != null ?
                    SubcategoryResponse.builder()
                        .id(expense.getSubcategory().getId())
                        .name(expense.getSubcategory().getName())
                        .categoryId(expense.getSubcategory().getCategory() != null
                                ? expense.getSubcategory().getCategory().getId() : null)
                        .build() : null)
                .paymentMethod(expense.getPaymentMethod())
                .paymentApp(expense.getPaymentApp() != null ?
                    PaymentAppResponse.builder()
                        .id(expense.getPaymentApp().getId())
                        .name(expense.getPaymentApp().getName())
                        .type(expense.getPaymentApp().getType())
                        .build() : null)
                .paymentSource(expense.getPaymentSource() != null ?
                    PaymentSourceResponse.builder()
                        .id(expense.getPaymentSource().getId())
                        .name(expense.getPaymentSource().getName())
                        .type(expense.getPaymentSource().getType())
                        .bankName(expense.getPaymentSource().getBankName())
                        .lastFourDigits(expense.getPaymentSource().getLastFourDigits())
                        .isActive(expense.getPaymentSource().getIsActive())
                        .build() : null)
                .notes(expense.getNotes())
                .purpose(expense.getPurpose())
                .isSplit(expense.getIsSplit())
                .splitWith(expense.getSplitWith())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}