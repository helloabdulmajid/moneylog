package in.abdulmajid.moneylog.user.dto.request;

import in.abdulmajid.moneylog.auth.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    private Gender gender;
}
