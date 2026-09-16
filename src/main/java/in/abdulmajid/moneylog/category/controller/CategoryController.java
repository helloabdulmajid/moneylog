package in.abdulmajid.moneylog.category.controller;

import in.abdulmajid.moneylog.category.dto.request.CategoryRequest;
import in.abdulmajid.moneylog.category.dto.request.SubcategoryRequest;
import in.abdulmajid.moneylog.category.dto.response.CategoryResponse;
import in.abdulmajid.moneylog.category.dto.response.SubcategoryResponse;
import in.abdulmajid.moneylog.category.service.CategoryService;
import in.abdulmajid.moneylog.common.CurrentUserHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserHelper currentUserHelper;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(categoryService.getAllCategories(userId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(categoryService.createCategory(userId, request));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(categoryService.updateCategory(userId, categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        categoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<SubcategoryResponse>> getSubcategories(
            @PathVariable UUID categoryId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(categoryService.getSubcategories(userId, categoryId));
    }

    @PostMapping("/{categoryId}/subcategories")
    public ResponseEntity<SubcategoryResponse> createSubcategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody SubcategoryRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(categoryService.createSubcategory(userId, categoryId, request));
    }

    @PutMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<SubcategoryResponse> updateSubcategory(
            @PathVariable UUID subcategoryId,
            @Valid @RequestBody SubcategoryRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(categoryService.updateSubcategory(userId, subcategoryId, request));
    }

    @DeleteMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable UUID subcategoryId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        categoryService.deleteSubcategory(userId, subcategoryId);
        return ResponseEntity.noContent().build();
    }
}
