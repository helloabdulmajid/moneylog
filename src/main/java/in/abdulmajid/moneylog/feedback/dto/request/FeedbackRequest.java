package in.abdulmajid.moneylog.feedback.dto.request;

import in.abdulmajid.moneylog.feedback.model.FeedbackCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotNull(message = "Category is required")
    private FeedbackCategory category;

    @NotBlank(message = "Subject is required")
    @Size(max = 150, message = "Subject must be 150 characters or fewer")
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(max = 10000, message = "Description must be 10000 characters or fewer")
    private String description;

    @Size(max = 5000, message = "Steps to reproduce must be 5000 characters or fewer")
    private String stepsToReproduce;

    @Email(message = "Enter a valid email address")
    @Size(max = 254, message = "Email must be 254 characters or fewer")
    private String contactEmail;
}