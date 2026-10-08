package in.abdulmajid.moneylog.feedback.repository;

import in.abdulmajid.moneylog.feedback.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID>, JpaSpecificationExecutor<Feedback> {

    @Modifying
    @Query("update Feedback f set f.user = null where f.user.id = :userId")
    int nullifyUserId(@Param("userId") UUID userId);

    @Query("select f.status, count(f) from Feedback f group by f.status")
    List<Object[]> countGroupByStatus();

    @Query("select f.category, count(f) from Feedback f group by f.category")
    List<Object[]> countGroupByCategory();

    @Query("select f.user.id, count(f) from Feedback f where f.user.id in :userIds group by f.user.id")
    List<Object[]> countGroupByUserIds(@Param("userIds") List<UUID> userIds);

    List<Feedback> findTop5ByOrderByCreatedAtDesc();

    Page<Feedback> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    default Map<UUID, Long> countByUserIdsAsMap(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return countGroupByUserIds(userIds).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));
    }
}
