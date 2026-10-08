package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.feedback.model.Feedback;
import in.abdulmajid.moneylog.feedback.model.FeedbackCategory;
import in.abdulmajid.moneylog.feedback.model.FeedbackStatus;
import in.abdulmajid.moneylog.feedback.model.NotificationStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminFeedbackSpecification implements Specification<Feedback> {

    private final FeedbackStatus status;
    private final FeedbackCategory category;
    private final NotificationStatus notificationStatus;
    private final String search;

    @Override
    public Predicate toPredicate(Root<Feedback> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (category != null) {
            predicates.add(cb.equal(root.get("category"), category));
        }
        if (notificationStatus != null) {
            predicates.add(cb.equal(root.get("notificationStatus"), notificationStatus));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            // LEFT join: anonymous feedback (user_id NULL) must stay searchable.
            jakarta.persistence.criteria.Join<Object, Object> userJoin =
                    root.join("user", jakarta.persistence.criteria.JoinType.LEFT);
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("subject")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("contactEmail")), pattern),
                    cb.like(cb.lower(userJoin.get("email")), pattern)
            ));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
