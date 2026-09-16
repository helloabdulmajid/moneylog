package in.abdulmajid.moneylog.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CategoryBreakdown {
    private String categoryName;
    private BigDecimal total;
    private BigDecimal percentage;
}
