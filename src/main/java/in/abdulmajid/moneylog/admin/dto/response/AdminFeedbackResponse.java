package in.abdulmajid.moneylog.admin.dto.response;

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
public class AdminFeedbackResponse {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String category;
    private String status;
    private String subject;
    private String description;
    private String stepsToReproduce;
    private String contactEmail;
    private Boolean hasScreenshot;
    private String notificationStatus;
    private String notificationError;
    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;
}
