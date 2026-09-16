package in.abdulmajid.moneylog.expense.repository;

import in.abdulmajid.moneylog.expense.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.isCreditCardBillPayment = false")
    BigDecimal sumByUserAndDateRange(@Param("userId") UUID userId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category.name as categoryName, SUM(e.amount) as total " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.isCreditCardBillPayment = false " +
           "GROUP BY e.category.name " +
           "ORDER BY total DESC")
    List<Object[]> categoryBreakdown(@Param("userId") UUID userId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT e.subcategory.name as subcategoryName, SUM(e.amount) as total " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.category.id = :categoryId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.isCreditCardBillPayment = false " +
           "GROUP BY e.subcategory.name " +
           "ORDER BY total DESC")
    List<Object[]> subcategoryBreakdown(@Param("userId") UUID userId,
                                        @Param("categoryId") UUID categoryId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT e.paymentAccount.name as accountName, SUM(e.amount) as total " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.isCreditCardBillPayment = false " +
           "GROUP BY e.paymentAccount.name " +
           "ORDER BY total DESC")
    List<Object[]> accountBreakdown(@Param("userId") UUID userId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    List<Expense> findByUserIdAndExpenseDateBetweenAndIsCreditCardBillPaymentFalseOrderByExpenseDateDescExpenseTimeDesc(
        UUID userId, LocalDate startDate, LocalDate endDate);

    List<Expense> findTop5ByUserIdAndIsCreditCardBillPaymentFalseOrderByExpenseDateDescExpenseTimeDesc(UUID userId);

    @Query("SELECT e FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.isCreditCardBillPayment = false " +
           "ORDER BY e.expenseDate DESC, e.expenseTime DESC")
    List<Expense> findRecentByUserId(@Param("userId") UUID userId);
}
