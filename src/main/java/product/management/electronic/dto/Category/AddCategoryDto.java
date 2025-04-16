package product.management.electronic.dto.Category;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCategoryDto {
    private String name;
    private String type;
}
