package in.abdulmajid.moneylog.expense.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ExpenseFilter {
    private Integer month;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID categoryId;
    private UUID subcategoryId;
    private String paymentMethod;
    private UUID paymentAppId;
    private UUID paymentAccountId;
    private Boolean isSplit;
    private String search;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;
}
