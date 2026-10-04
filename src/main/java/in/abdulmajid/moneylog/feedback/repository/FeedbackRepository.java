package in.abdulmajid.moneylog.feedback.repository;

import in.abdulmajid.moneylog.feedback.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    @Modifying
    @Query("update Feedback f set f.user = null where f.user.id = :userId")
    int nullifyUserId(@Param("userId") UUID userId);
}