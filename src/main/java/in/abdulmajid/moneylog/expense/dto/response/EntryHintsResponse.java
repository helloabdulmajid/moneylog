package in.abdulmajid.moneylog.expense.dto.response;

import in.abdulmajid.moneylog.category.dto.response.CategoryResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentSourceResponse;
import in.abdulmajid.moneylog.payment.model.PaymentMethod;
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
        private PaymentMethod paymentMethod;
        private UUID paymentAppId;
        private UUID paymentSourceId;
        private UUID categoryId;
        private UUID subcategoryId;
    }

    @Data
    @Builder
    public static class Frequent {
        private List<CategoryResponse> categories;
        private List<PaymentMethod> paymentMethods;
        private List<PaymentAppResponse> apps;
        private List<PaymentSourceResponse> sources;
    }
}