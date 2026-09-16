package in.abdulmajid.moneylog.expense.dto.response;

import in.abdulmajid.moneylog.category.dto.response.CategoryResponse;
import in.abdulmajid.moneylog.expense.model.Expense;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAccountResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EntryHintsResponse {

    private LastUsed lastUsed;
    private Frequent frequent;

    @Data
    @Builder
    public static class LastUsed {
        private Expense.PaymentMethod paymentMethod;
        private UUID paymentAppId;
        private UUID paymentAccountId;
        private UUID categoryId;
        private UUID subcategoryId;
    }

    @Data
    @Builder
    public static class Frequent {
        private List<CategoryResponse> categories;
        private List<Expense.PaymentMethod> paymentMethods;
        private List<PaymentAppResponse> apps;
        private List<PaymentAccountResponse> accounts;
    }
}