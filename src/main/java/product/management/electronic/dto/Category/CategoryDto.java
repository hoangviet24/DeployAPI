package product.management.electronic.dto.Category;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private UUID id;
    private String name;
    private String type;
}
