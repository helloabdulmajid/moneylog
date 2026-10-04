package in.abdulmajid.moneylog.auth.service;

/**
 * Outcome of a feedback notification email attempt.
 *
 * <p>SENT means SMTP accepted the message. LOGGED means the development
 * log-fallback wrote the message to the server log instead (no real email was
 * sent). FAILED means a real send attempt errored.</p>
 */
public record MailDeliveryResult(MailStatus status, String error) {

    public enum MailStatus {
        SENT,
        LOGGED,
        FAILED
    }

    public static MailDeliveryResult sent() {
        return new MailDeliveryResult(MailStatus.SENT, null);
    }

    public static MailDeliveryResult logged(String reason) {
        return new MailDeliveryResult(MailStatus.LOGGED, reason);
    }

    public static MailDeliveryResult failed(String error) {
        return new MailDeliveryResult(MailStatus.FAILED, error);
    }
}