package in.abdulmajid.moneylog.category.service;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.category.dto.request.CategoryRequest;
import in.abdulmajid.moneylog.category.dto.request.SubcategoryRequest;
import in.abdulmajid.moneylog.category.dto.response.CategoryResponse;
import in.abdulmajid.moneylog.category.dto.response.SubcategoryResponse;
import in.abdulmajid.moneylog.category.model.Category;
import in.abdulmajid.moneylog.category.model.Subcategory;
import in.abdulmajid.moneylog.category.repository.CategoryRepository;
import in.abdulmajid.moneylog.category.repository.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final UserRepository userRepository;

    public List<CategoryResponse> getAllCategories(UUID userId) {
        return categoryRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(UUID userId, CategoryRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        if (categoryRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(UUID userId, UUID categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(category);
    }

    public List<SubcategoryResponse> getSubcategories(UUID userId, UUID categoryId) {
        return subcategoryRepository.findByCategoryIdAndUserIdOrderByCategoryId(categoryId, userId).stream()
                .map(this::toSubcategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubcategoryResponse createSubcategory(UUID userId, UUID categoryId, SubcategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Category not found"));

        User user = userRepository.findById(userId).orElseThrow();

        if (subcategoryRepository.existsByCategoryIdAndName(categoryId, request.getName())) {
            throw new RuntimeException("Subcategory already exists");
        }

        Subcategory subcategory = Subcategory.builder()
                .category(category)
                .user(user)
                .name(request.getName())
                .build();

        return toSubcategoryResponse(subcategoryRepository.save(subcategory));
    }

    @Transactional
    public SubcategoryResponse updateSubcategory(UUID userId, UUID subcategoryId, SubcategoryRequest request) {
        Subcategory subcategory = subcategoryRepository.findById(subcategoryId)
                .filter(s -> s.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        subcategory.setName(request.getName());
        return toSubcategoryResponse(subcategoryRepository.save(subcategory));
    }

    @Transactional
    public void deleteSubcategory(UUID userId, UUID subcategoryId) {
        Subcategory subcategory = subcategoryRepository.findById(subcategoryId)
                .filter(s -> s.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));
        subcategoryRepository.delete(subcategory);
    }

    private CategoryResponse toCategoryResponse(Category category) {
        List<SubcategoryResponse> subcategories = category.getSubcategories().stream()
                .map(this::toSubcategoryResponse)
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .sortOrder(category.getSortOrder())
                .subcategories(subcategories)
                .build();
    }

    private SubcategoryResponse toSubcategoryResponse(Subcategory subcategory) {
        return SubcategoryResponse.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
                .categoryId(subcategory.getCategory().getId())
                .build();
    }
}
