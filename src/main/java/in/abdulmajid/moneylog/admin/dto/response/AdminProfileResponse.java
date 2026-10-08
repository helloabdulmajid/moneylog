package in.abdulmajid.moneylog.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileResponse {

    private UUID id;
    private String email;
    private String name;
    private String role;
    private List<String> permissions;
    private Boolean active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
