package in.abdulmajid.moneylog.expense.controller;

import in.abdulmajid.moneylog.common.CurrentUserHelper;
import in.abdulmajid.moneylog.expense.dto.request.ExpenseFilter;
import in.abdulmajid.moneylog.expense.dto.request.ExpenseRequest;
import in.abdulmajid.moneylog.expense.dto.response.EntryHintsResponse;
import in.abdulmajid.moneylog.expense.dto.response.ExpenseResponse;
import in.abdulmajid.moneylog.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUserHelper currentUserHelper;

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getExpenses(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID subcategoryId,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) UUID paymentAppId,
            @RequestParam(required = false) UUID paymentSourceId,
            @RequestParam(required = false) Boolean isSplit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        UUID userId = currentUserHelper.getCurrentUserId();

        ExpenseFilter filter = new ExpenseFilter();
        filter.setMonth(month);
        filter.setYear(year);
        if (startDate != null) filter.setStartDate(java.time.LocalDate.parse(startDate));
        if (endDate != null) filter.setEndDate(java.time.LocalDate.parse(endDate));
        filter.setCategoryId(categoryId);
        filter.setSubcategoryId(subcategoryId);
        filter.setPaymentMethod(paymentMethod);
        filter.setPaymentAppId(paymentAppId);
        filter.setPaymentSourceId(paymentSourceId);
        filter.setIsSplit(isSplit);
        filter.setSearch(search);
        filter.setSortBy(sortBy);
        filter.setSortOrder(sortOrder);
        filter.setPage(page);
        filter.setSize(size);

        return ResponseEntity.ok(expenseService.getExpenses(filter, userId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ExpenseResponse>> getRecentExpenses() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(expenseService.getRecentExpenses(userId));
    }

    @GetMapping("/entry-hints")
    public ResponseEntity<EntryHintsResponse> getEntryHints() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(expenseService.getEntryHints(userId));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable UUID expenseId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(expenseService.getExpenseById(userId, expenseId));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(expenseService.createExpense(userId, request));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(expenseService.updateExpense(userId, expenseId, request));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        expenseService.deleteExpense(userId, expenseId);
        return ResponseEntity.noContent().build();
    }
}
