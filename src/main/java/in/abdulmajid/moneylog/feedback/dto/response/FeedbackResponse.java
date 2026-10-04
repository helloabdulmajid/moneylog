package in.abdulmajid.moneylog.feedback.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private UUID id;
    private String category;
    private String status;
    private String subject;
    private String notificationStatus;
    private LocalDateTime submittedAt;
    private String message;
}