package in.abdulmajid.moneylog.payment.dto.response;

import in.abdulmajid.moneylog.payment.model.PaymentSource;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentSourceResponse {
    private UUID id;
    private String name;
    private PaymentSource.PaymentSourceType type;
    private String bankName;
    private String lastFourDigits;
    private Boolean isActive;
}