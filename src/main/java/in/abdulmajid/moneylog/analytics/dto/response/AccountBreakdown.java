package in.abdulmajid.moneylog.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountBreakdown {
    private String accountName;
    private BigDecimal total;
    private BigDecimal percentage;
}
