package in.abdulmajid.moneylog.payment.repository;

import in.abdulmajid.moneylog.payment.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
    List<CreditCard> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<CreditCard> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);
    boolean existsByUserIdAndIssuerAndLastFourDigits(UUID userId, String issuer, String lastFourDigits);
    Optional<CreditCard> findByUserIdAndIssuerAndLastFourDigits(UUID userId, String issuer, String lastFourDigits);
    void deleteByUserId(UUID userId);
}