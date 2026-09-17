package in.abdulmajid.moneylog.payment.dto.request;

import in.abdulmajid.moneylog.payment.model.PaymentSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentSourceRequest {

    @NotBlank(message = "Source name is required")
    private String name;

    @NotNull(message = "Source type is required")
    private PaymentSource.PaymentSourceType type;

    private String bankName;

    private String lastFourDigits;

    private Boolean isActive;
}