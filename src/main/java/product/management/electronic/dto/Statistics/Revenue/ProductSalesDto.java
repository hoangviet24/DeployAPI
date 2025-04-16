package product.management.electronic.dto.Statistics.Revenue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSalesDto {
    private UUID productId;
    private String productName;
    private String thumbnail;
    private int quantitySold;
    private double totalRevenue;
}
