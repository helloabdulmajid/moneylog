package in.abdulmajid.moneylog.category.repository;

import in.abdulmajid.moneylog.category.model.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubcategoryRepository extends JpaRepository<Subcategory, UUID> {
    List<Subcategory> findByCategoryIdAndUserIdOrderByCategoryId(UUID categoryId, UUID userId);
    boolean existsByCategoryIdAndName(UUID categoryId, String name);
    void deleteByUserId(UUID userId);
}
