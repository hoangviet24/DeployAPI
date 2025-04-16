package product.management.electronic.dto.Statistics.Revenue;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RevenueByDateDto {
    private LocalDate date;
    private double totalRevenue;
}
