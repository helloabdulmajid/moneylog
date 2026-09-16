package in.abdulmajid.moneylog.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MonthlySummary {
    private BigDecimal totalExpense;
    private Integer month;
    private Integer year;
    private BigDecimal previousMonthTotal;
    private BigDecimal percentChange;
    private List<CategoryBreakdown> categoryBreakdown;
    private List<AccountBreakdown> accountBreakdown;
}
