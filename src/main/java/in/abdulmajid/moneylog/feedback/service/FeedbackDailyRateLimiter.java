package in.abdulmajid.moneylog.feedback.service;

import in.abdulmajid.moneylog.feedback.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FeedbackDailyRateLimiter {

    private static final int MAX_SUBMISSIONS_PER_DAY = 10;

    private final ConcurrentHashMap<String, Deque<LocalDate>> submissions = new ConcurrentHashMap<>();

    public void check(String key) {
        LocalDate today = LocalDate.now();
        submissions.compute(key, (k, deque) -> {
            Deque<LocalDate> queue = deque != null ? deque : new ArrayDeque<>();
            while (!queue.isEmpty() && queue.peekFirst().isBefore(today)) {
                queue.pollFirst();
            }
            if (queue.size() >= MAX_SUBMISSIONS_PER_DAY) {
                throw new RateLimitExceededException(
                        "Daily feedback limit reached (10 per day). Please try again tomorrow.");
            }
            queue.addLast(today);
            return queue;
        });
    }
}
