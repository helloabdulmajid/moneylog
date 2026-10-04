package in.abdulmajid.moneylog.feedback.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Too many submissions. Please try again later.");
    }
}