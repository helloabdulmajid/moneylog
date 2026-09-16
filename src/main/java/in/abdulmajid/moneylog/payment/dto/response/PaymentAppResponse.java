package in.abdulmajid.moneylog.payment.dto.response;

import in.abdulmajid.moneylog.payment.model.PaymentApp;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentAppResponse {
    private UUID id;
    private String name;
    private PaymentApp.PaymentAppType type;
}
