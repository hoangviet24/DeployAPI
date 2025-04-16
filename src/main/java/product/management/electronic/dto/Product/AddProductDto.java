package product.management.electronic.dto.Product;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProductDto {
    private String name;
    private String sku;
    private String description;
    private String brand;
    private BigDecimal price;
    private int quantity;
    private String thumbnail;
    private List<String> images;
    private UUID categoryId;
    private boolean isFeatured;

}
