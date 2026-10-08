package in.abdulmajid.moneylog.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateFlagRequest {

    @NotBlank(message = "Key is required")
    @Pattern(regexp = "^[a-z][a-z0-9_.:-]{1,63}$",
            message = "Key must start with a lowercase letter and contain only lowercase letters, digits, _, ., :, - (max 64 chars)")
    private String key;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private Boolean enabled;
}
