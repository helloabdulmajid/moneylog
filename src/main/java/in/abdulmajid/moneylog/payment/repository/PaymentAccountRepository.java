package in.abdulmajid.moneylog.payment.repository;

import in.abdulmajid.moneylog.payment.model.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {
    List<PaymentAccount> findByUserIdAndIsActiveTrueOrderByNameAsc(UUID userId);
    List<PaymentAccount> findByUserIdOrderByNameAsc(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
}
