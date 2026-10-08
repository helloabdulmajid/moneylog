package in.abdulmajid.moneylog.featureflag.repository;

import in.abdulmajid.moneylog.featureflag.model.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

    Optional<FeatureFlag> findByKey(String key);

    List<FeatureFlag> findByEnabledTrue();

    long countByEnabledTrue();
}
