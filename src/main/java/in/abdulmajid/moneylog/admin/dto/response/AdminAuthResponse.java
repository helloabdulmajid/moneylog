package in.abdulmajid.moneylog.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private AdminProfileResponse admin;
}
