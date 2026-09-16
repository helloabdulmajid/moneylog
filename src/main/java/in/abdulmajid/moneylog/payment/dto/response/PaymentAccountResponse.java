package in.abdulmajid.moneylog.payment.dto.response;

import in.abdulmajid.moneylog.payment.model.PaymentAccount;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentAccountResponse {
    private UUID id;
    private String name;
    private PaymentAccount.AccountType type;
    private String bankName;
    private String lastFourDigits;
    private Boolean isActive;
}
