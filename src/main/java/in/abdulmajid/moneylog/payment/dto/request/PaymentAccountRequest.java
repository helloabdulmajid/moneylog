package in.abdulmajid.moneylog.payment.dto.request;

import in.abdulmajid.moneylog.payment.model.PaymentAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentAccountRequest {

    @NotBlank(message = "Account name is required")
    private String name;

    @NotNull(message = "Account type is required")
    private PaymentAccount.AccountType type;

    private String bankName;

    private String lastFourDigits;
}
