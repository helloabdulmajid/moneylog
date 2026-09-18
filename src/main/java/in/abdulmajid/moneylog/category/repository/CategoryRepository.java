package in.abdulmajid.moneylog.category.repository;

import in.abdulmajid.moneylog.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserIdOrderBySortOrderAsc(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
    void deleteByUserId(UUID userId);
}
