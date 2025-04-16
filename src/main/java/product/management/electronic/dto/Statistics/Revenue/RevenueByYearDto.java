package product.management.electronic.dto.Statistics.Revenue;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueByYearDto {
    private int month;
    private double totalRevenue;
}
