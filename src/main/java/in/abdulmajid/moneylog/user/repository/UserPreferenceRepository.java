package in.abdulmajid.moneylog.user.repository;

import in.abdulmajid.moneylog.user.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {
    Optional<UserPreference> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
