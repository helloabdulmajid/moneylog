package in.abdulmajid.moneylog.payment.dto.response;

import in.abdulmajid.moneylog.payment.model.CardNetwork;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CreditCardResponse {
    private UUID id;
    private String name;
    private String issuer;
    private String lastFourDigits;
    private CardNetwork network;
    private BigDecimal creditLimit;
    private Boolean isActive;
    private UUID paymentSourceId;
}