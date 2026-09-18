package in.abdulmajid.moneylog.expense.repository;

import in.abdulmajid.moneylog.expense.model.Expense;
import in.abdulmajid.moneylog.payment.model.PaymentMethod;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndDateRange(@Param("userId") UUID userId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category.name as categoryName, SUM(e.amount) as total " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
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
           "GROUP BY e.subcategory.name " +
           "ORDER BY total DESC")
    List<Object[]> subcategoryBreakdown(@Param("userId") UUID userId,
                                        @Param("categoryId") UUID categoryId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT e.paymentSource.name as sourceName, SUM(e.amount) as total " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.paymentSource.name " +
           "ORDER BY total DESC")
    List<Object[]> sourceBreakdown(@Param("userId") UUID userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDescExpenseTimeDesc(
        UUID userId, LocalDate startDate, LocalDate endDate);

    List<Expense> findByUserIdAndPaymentSource_IdOrderByExpenseDateDescExpenseTimeDesc(UUID userId, UUID sourceId, Pageable pageable);

    List<Expense> findTop5ByUserIdOrderByExpenseDateDescExpenseTimeDesc(UUID userId);

    @Query("SELECT e FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "ORDER BY e.expenseDate DESC, e.expenseTime DESC")
    List<Expense> findRecentByUserId(@Param("userId") UUID userId);

    Optional<Expense> findFirstByUserIdOrderByExpenseDateDescExpenseTimeDesc(UUID userId);

    @Query("SELECT e.category.id, e.category.name, e.category.icon, e.category.color, COUNT(e) as cnt " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.category IS NOT NULL " +
           "GROUP BY e.category.id, e.category.name, e.category.icon, e.category.color " +
           "ORDER BY cnt DESC")
    List<Object[]> findFrequentCategories(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT e.paymentMethod, COUNT(e) as cnt " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.paymentMethod IS NOT NULL " +
           "GROUP BY e.paymentMethod " +
           "ORDER BY cnt DESC")
    List<Object[]> findFrequentPaymentMethods(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT e.paymentApp.id, e.paymentApp.name, e.paymentApp.type, COUNT(e) as cnt " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.paymentApp IS NOT NULL " +
           "GROUP BY e.paymentApp.id, e.paymentApp.name, e.paymentApp.type " +
           "ORDER BY cnt DESC")
    List<Object[]> findFrequentPaymentApps(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT e.paymentSource.id, e.paymentSource.name, e.paymentSource.type, " +
           "e.paymentSource.bankName, e.paymentSource.lastFourDigits, e.paymentSource.isActive, COUNT(e) as cnt " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.paymentSource IS NOT NULL " +
           "GROUP BY e.paymentSource.id, e.paymentSource.name, e.paymentSource.type, " +
           "e.paymentSource.bankName, e.paymentSource.lastFourDigits, e.paymentSource.isActive " +
           "ORDER BY cnt DESC")
    List<Object[]> findFrequentPaymentSources(@Param("userId") UUID userId, Pageable pageable);

    void deleteByUserId(UUID userId);
}