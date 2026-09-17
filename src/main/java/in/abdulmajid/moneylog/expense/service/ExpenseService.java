package in.abdulmajid.moneylog.expense.service;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.category.dto.response.CategoryResponse;
import in.abdulmajid.moneylog.category.dto.response.SubcategoryResponse;
import in.abdulmajid.moneylog.category.model.Category;
import in.abdulmajid.moneylog.category.model.Subcategory;
import in.abdulmajid.moneylog.category.repository.CategoryRepository;
import in.abdulmajid.moneylog.category.repository.SubcategoryRepository;
import in.abdulmajid.moneylog.expense.dto.request.ExpenseFilter;
import in.abdulmajid.moneylog.expense.dto.request.ExpenseRequest;
import in.abdulmajid.moneylog.expense.dto.response.EntryHintsResponse;
import in.abdulmajid.moneylog.expense.dto.response.ExpenseResponse;
import in.abdulmajid.moneylog.expense.model.Expense;
import in.abdulmajid.moneylog.expense.repository.ExpenseRepository;
import in.abdulmajid.moneylog.expense.repository.ExpenseSpecification;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentSourceResponse;
import in.abdulmajid.moneylog.payment.model.PaymentApp;
import in.abdulmajid.moneylog.payment.model.PaymentMethod;
import in.abdulmajid.moneylog.payment.model.PaymentSource;
import in.abdulmajid.moneylog.payment.repository.PaymentAppRepository;
import in.abdulmajid.moneylog.payment.repository.PaymentSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final PaymentAppRepository paymentAppRepository;
    private final PaymentSourceRepository paymentSourceRepository;

    public Page<ExpenseResponse> getExpenses(ExpenseFilter filter, UUID userId) {
        Specification<Expense> spec = new ExpenseSpecification(filter, userId);
        
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        
        return expenseRepository.findAll(spec, PageRequest.of(page, size))
                .map(this::toExpenseResponse);
    }

    public List<ExpenseResponse> getRecentExpenses(UUID userId) {
        return expenseRepository.findTop5ByUserIdOrderByExpenseDateDescExpenseTimeDesc(userId)
                .stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }

    public EntryHintsResponse getEntryHints(UUID userId) {
        EntryHintsResponse.LastUsed lastUsed = expenseRepository
                .findFirstByUserIdOrderByExpenseDateDescExpenseTimeDesc(userId)
                .map(e -> EntryHintsResponse.LastUsed.builder()
                        .paymentMethod(e.getPaymentMethod())
                        .paymentAppId(e.getPaymentApp() != null ? e.getPaymentApp().getId() : null)
                        .paymentSourceId(e.getPaymentSource() != null ? e.getPaymentSource().getId() : null)
                        .categoryId(e.getCategory() != null ? e.getCategory().getId() : null)
                        .subcategoryId(e.getSubcategory() != null ? e.getSubcategory().getId() : null)
                        .build())
                .orElse(null);

        Pageable catLimit = PageRequest.of(0, 6);
        Pageable restLimit = PageRequest.of(0, 4);

        EntryHintsResponse.Frequent frequent = EntryHintsResponse.Frequent.builder()
                .categories(expenseRepository.findFrequentCategories(userId, catLimit).stream()
                        .map(row -> CategoryResponse.builder()
                                .id((UUID) row[0])
                                .name((String) row[1])
                                .icon((String) row[2])
                                .color((String) row[3])
                                .build())
                        .collect(Collectors.toList()))
                .paymentMethods(expenseRepository.findFrequentPaymentMethods(userId, restLimit).stream()
                        .map(row -> (PaymentMethod) row[0])
                        .collect(Collectors.toList()))
                .apps(expenseRepository.findFrequentPaymentApps(userId, restLimit).stream()
                        .map(row -> PaymentAppResponse.builder()
                                .id((UUID) row[0])
                                .name((String) row[1])
                                .type((PaymentApp.PaymentAppType) row[2])
                                .build())
                        .collect(Collectors.toList()))
                .sources(expenseRepository.findFrequentPaymentSources(userId, restLimit).stream()
                        .map(row -> PaymentSourceResponse.builder()
                                .id((UUID) row[0])
                                .name((String) row[1])
                                .type((PaymentSource.PaymentSourceType) row[2])
                                .bankName((String) row[3])
                                .lastFourDigits((String) row[4])
                                .isActive(row[5] != null ? (Boolean) row[5] : null)
                                .build())
                        .collect(Collectors.toList()))
                .build();

        return EntryHintsResponse.builder()
                .lastUsed(lastUsed)
                .frequent(frequent)
                .build();
    }

    public ExpenseResponse getExpenseById(UUID userId, UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        return toExpenseResponse(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(UUID userId, ExpenseRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        if (request.getCategoryId() == null) {
            throw new RuntimeException("Category is required");
        }

        Expense expense = Expense.builder()
                .user(user)
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now())
                .expenseTime(request.getExpenseTime() != null ? request.getExpenseTime() : LocalTime.now())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .purpose(request.getPurpose())
                .isSplit(request.getIsSplit() != null ? request.getIsSplit() : false)
                .splitWith(request.getSplitWith())
                .build();

        // Set category (required)
        {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            expense.setCategory(category);
        }

        // Set subcategory if provided
        if (request.getSubcategoryId() != null) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .filter(s -> s.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Subcategory not found"));
            expense.setSubcategory(subcategory);
        }

        // Set payment app if provided
        if (request.getPaymentAppId() != null) {
            PaymentApp paymentApp = paymentAppRepository.findById(request.getPaymentAppId())
                    .filter(a -> a.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Payment app not found"));
            expense.setPaymentApp(paymentApp);
        }

        // Set payment source if provided
        if (request.getPaymentSourceId() != null) {
            PaymentSource paymentSource = paymentSourceRepository.findById(request.getPaymentSourceId())
                    .filter(a -> a.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Payment source not found"));
            expense.setPaymentSource(paymentSource);
        }

        return toExpenseResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse updateExpense(UUID userId, UUID expenseId, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : expense.getExpenseDate());
        expense.setExpenseTime(request.getExpenseTime() != null ? request.getExpenseTime() : expense.getExpenseTime());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setNotes(request.getNotes());
        expense.setPurpose(request.getPurpose());
        expense.setIsSplit(request.getIsSplit() != null ? request.getIsSplit() : expense.getIsSplit());
        expense.setSplitWith(request.getSplitWith());

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            expense.setCategory(category);
        }

        // Update subcategory if provided
        if (request.getSubcategoryId() != null) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .filter(s -> s.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Subcategory not found"));
            expense.setSubcategory(subcategory);
        }

        // Update payment app if provided
        if (request.getPaymentAppId() != null) {
            PaymentApp paymentApp = paymentAppRepository.findById(request.getPaymentAppId())
                    .filter(a -> a.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Payment app not found"));
            expense.setPaymentApp(paymentApp);
        }

        // Update payment source if provided
        if (request.getPaymentSourceId() != null) {
            PaymentSource paymentSource = paymentSourceRepository.findById(request.getPaymentSourceId())
                    .filter(a -> a.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Payment source not found"));
            expense.setPaymentSource(paymentSource);
        }

        return toExpenseResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(UUID userId, UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        expenseRepository.delete(expense);
    }

    private ExpenseResponse toExpenseResponse(Expense expense) {
        return ExpenseResponseMapper.map(expense);
    }
}
