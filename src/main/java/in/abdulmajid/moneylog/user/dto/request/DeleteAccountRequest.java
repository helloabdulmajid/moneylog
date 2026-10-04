package in.abdulmajid.moneylog.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;
}