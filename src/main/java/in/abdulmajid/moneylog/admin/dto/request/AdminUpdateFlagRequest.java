package in.abdulmajid.moneylog.admin.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateFlagRequest {

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private Boolean enabled;
}
