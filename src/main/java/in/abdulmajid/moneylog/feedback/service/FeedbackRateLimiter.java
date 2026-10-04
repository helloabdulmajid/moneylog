package in.abdulmajid.moneylog.feedback.service;

import in.abdulmajid.moneylog.feedback.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FeedbackRateLimiter {

    private static final int MAX_SUBMISSIONS_PER_WINDOW = 10;
    private static final Duration WINDOW = Duration.ofHours(1);

    private final ConcurrentHashMap<String, Deque<Long>> submissions = new ConcurrentHashMap<>();

    public void check(String key) {
        long now = System.currentTimeMillis();
        long cutoff = now - WINDOW.toMillis();
        submissions.compute(key, (k, deque) -> {
            Deque<Long> queue = deque != null ? deque : new ArrayDeque<>();
            while (!queue.isEmpty() && queue.peekFirst() < cutoff) {
                queue.pollFirst();
            }
            if (queue.size() >= MAX_SUBMISSIONS_PER_WINDOW) {
                throw new RateLimitExceededException();
            }
            queue.addLast(now);
            return queue;
        });
    }
}