package in.abdulmajid.moneylog.payment.dto.request;

import in.abdulmajid.moneylog.payment.model.PaymentApp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentAppRequest {

    @NotBlank(message = "Payment app name is required")
    private String name;

    @NotNull(message = "Payment app type is required")
    private PaymentApp.PaymentAppType type;
}
