package in.abdulmajid.moneylog.payment.repository;

import in.abdulmajid.moneylog.payment.model.PaymentSource;
import in.abdulmajid.moneylog.payment.model.PaymentSource.PaymentSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentSourceRepository extends JpaRepository<PaymentSource, UUID> {
    List<PaymentSource> findByUserIdAndIsActiveTrueOrderByNameAsc(UUID userId);
    List<PaymentSource> findByUserIdOrderByNameAsc(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
    void deleteByUserId(UUID userId);

    @Query("SELECT s FROM PaymentSource s " +
           "WHERE s.user.id = :userId AND s.type = :type " +
           "AND s.lastFourDigits = :lastFourDigits " +
           "AND (:bankName IS NULL OR s.bankName = :bankName)")
    Optional<PaymentSource> findCardSource(@Param("userId") UUID userId,
                                           @Param("type") PaymentSourceType type,
                                           @Param("bankName") String bankName,
                                           @Param("lastFourDigits") String lastFourDigits);
}