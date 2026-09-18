package in.abdulmajid.moneylog.user.dto.response;

import in.abdulmajid.moneylog.auth.model.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String name;
    private String email;
    private Gender gender;
    private LocalDateTime createdAt;
}
