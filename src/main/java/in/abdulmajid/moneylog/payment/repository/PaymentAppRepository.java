package in.abdulmajid.moneylog.payment.repository;

import in.abdulmajid.moneylog.payment.model.PaymentApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentAppRepository extends JpaRepository<PaymentApp, UUID> {
    List<PaymentApp> findByUserIdOrderByNameAsc(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
    void deleteByUserId(UUID userId);
}
