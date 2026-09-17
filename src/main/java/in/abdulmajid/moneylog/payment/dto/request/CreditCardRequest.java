package in.abdulmajid.moneylog.payment.dto.request;

import in.abdulmajid.moneylog.payment.model.CardNetwork;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditCardRequest {

    @NotBlank(message = "Card name is required")
    private String name;

    private String issuer;

    private String lastFourDigits;

    private CardNetwork network;

    private BigDecimal creditLimit;

    private Boolean isActive;
}