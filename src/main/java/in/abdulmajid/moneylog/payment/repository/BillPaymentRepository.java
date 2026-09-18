package in.abdulmajid.moneylog.payment.repository;

import in.abdulmajid.moneylog.payment.model.BillPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface BillPaymentRepository extends JpaRepository<BillPayment, UUID> {
    Page<BillPayment> findByUserIdOrderByPaymentDateDescPaymentTimeDesc(UUID userId, Pageable pageable);
    Page<BillPayment> findByUserIdAndCreditCardIdOrderByPaymentDateDescPaymentTimeDesc(UUID userId, UUID creditCardId, Pageable pageable);
    Page<BillPayment> findByUserIdAndPaymentDateBetweenOrderByPaymentDateDescPaymentTimeDesc(UUID userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<BillPayment> findByUserIdAndCreditCardIdAndPaymentDateBetweenOrderByPaymentDateDescPaymentTimeDesc(UUID userId, UUID creditCardId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    void deleteByUserId(UUID userId);
}