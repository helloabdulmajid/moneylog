package in.abdulmajid.moneylog.category.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SubcategoryResponse {
    private UUID id;
    private String name;
    private UUID categoryId;
}
