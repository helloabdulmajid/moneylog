package in.abdulmajid.moneylog.expense.repository;

import in.abdulmajid.moneylog.expense.dto.request.ExpenseFilter;
import in.abdulmajid.moneylog.expense.model.Expense;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExpenseSpecification implements Specification<Expense> {

    private final ExpenseFilter filter;
    private final UUID userId;

    public ExpenseSpecification(ExpenseFilter filter, UUID userId) {
        this.filter = filter;
        this.userId = userId;
    }

    @Override
    public Predicate toPredicate(Root<Expense> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // User filter
        predicates.add(cb.equal(root.get("user").get("id"), userId));

        // Exclude credit card bill payments from regular expense list
        predicates.add(cb.equal(root.get("isCreditCardBillPayment"), false));

        // Date filters
        if (filter.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), filter.getStartDate()));
        }
        if (filter.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), filter.getEndDate()));
        }

        // Month/Year filter
        if (filter.getMonth() != null && filter.getYear() != null) {
            predicates.add(cb.equal(cb.function("EXTRACT", Integer.class, 
                cb.literal("MONTH"), root.get("expenseDate")), filter.getMonth()));
            predicates.add(cb.equal(cb.function("EXTRACT", Integer.class, 
                cb.literal("YEAR"), root.get("expenseDate")), filter.getYear()));
        }

        // Category filter
        if (filter.getCategoryId() != null) {
            predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
        }

        // Subcategory filter
        if (filter.getSubcategoryId() != null) {
            predicates.add(cb.equal(root.get("subcategory").get("id"), filter.getSubcategoryId()));
        }

        // Payment method filter
        if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isEmpty()) {
            predicates.add(cb.equal(root.get("paymentMethod"), 
                Expense.PaymentMethod.valueOf(filter.getPaymentMethod())));
        }

        // Payment app filter
        if (filter.getPaymentAppId() != null) {
            predicates.add(cb.equal(root.get("paymentApp").get("id"), filter.getPaymentAppId()));
        }

        // Payment account filter
        if (filter.getPaymentAccountId() != null) {
            predicates.add(cb.equal(root.get("paymentAccount").get("id"), filter.getPaymentAccountId()));
        }

        // Split filter
        if (filter.getIsSplit() != null) {
            predicates.add(cb.equal(root.get("isSplit"), filter.getIsSplit()));
        }

        // Search in notes and purpose
        if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
            String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("notes")), searchPattern),
                cb.like(cb.lower(root.get("purpose")), searchPattern)
            ));
        }

        // Sorting
        if (filter.getSortBy() != null && !filter.getSortBy().isEmpty()) {
            Path<?> sortPath = root.get(filter.getSortBy());
            if ("desc".equalsIgnoreCase(filter.getSortOrder())) {
                query.orderBy(cb.desc(sortPath));
            } else {
                query.orderBy(cb.asc(sortPath));
            }
        } else {
            // Default sort by date desc, time desc
            query.orderBy(cb.desc(root.get("expenseDate")), cb.desc(root.get("expenseTime")));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
