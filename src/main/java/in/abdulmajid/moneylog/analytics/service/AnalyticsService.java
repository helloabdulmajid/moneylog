package in.abdulmajid.moneylog.analytics.service;

import in.abdulmajid.moneylog.analytics.dto.response.AccountBreakdown;
import in.abdulmajid.moneylog.analytics.dto.response.CategoryBreakdown;
import in.abdulmajid.moneylog.analytics.dto.response.MonthlySummary;
import in.abdulmajid.moneylog.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;

    public MonthlySummary getMonthlySummary(UUID userId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Current month total
        BigDecimal totalExpense = expenseRepository.sumByUserAndDateRange(userId, startDate, endDate);

        // Previous month total
        YearMonth previousMonth = yearMonth.minusMonths(1);
        BigDecimal previousMonthTotal = expenseRepository.sumByUserAndDateRange(
                userId, 
                previousMonth.atDay(1), 
                previousMonth.atEndOfMonth()
        );

        // Calculate percent change
        BigDecimal percentChange = BigDecimal.ZERO;
        if (previousMonthTotal.compareTo(BigDecimal.ZERO) > 0) {
            percentChange = totalExpense.subtract(previousMonthTotal)
                    .divide(previousMonthTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        // Category breakdown
        List<Object[]> categoryData = expenseRepository.categoryBreakdown(userId, startDate, endDate);
        List<CategoryBreakdown> categoryBreakdown = new ArrayList<>();
        for (Object[] row : categoryData) {
            String categoryName = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            BigDecimal percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0 ?
                    total.divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(1, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
            categoryBreakdown.add(CategoryBreakdown.builder()
                    .categoryName(categoryName)
                    .total(total)
                    .percentage(percentage)
                    .build());
        }

        // Source breakdown
        List<Object[]> sourceData = expenseRepository.sourceBreakdown(userId, startDate, endDate);
        List<AccountBreakdown> sourceBreakdown = new ArrayList<>();
        for (Object[] row : sourceData) {
            String sourceName = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            BigDecimal percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0 ?
                    total.divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(1, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
            sourceBreakdown.add(AccountBreakdown.builder()
                    .accountName(sourceName)
                    .total(total)
                    .percentage(percentage)
                    .build());
        }

        return MonthlySummary.builder()
                .totalExpense(totalExpense)
                .month(month)
                .year(year)
                .previousMonthTotal(previousMonthTotal)
                .percentChange(percentChange)
                .categoryBreakdown(categoryBreakdown)
                .accountBreakdown(sourceBreakdown)
                .build();
    }
}
