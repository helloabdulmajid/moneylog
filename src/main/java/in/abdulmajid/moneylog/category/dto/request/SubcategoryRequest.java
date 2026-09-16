package in.abdulmajid.moneylog.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubcategoryRequest {

    @NotBlank(message = "Subcategory name is required")
    private String name;
}
