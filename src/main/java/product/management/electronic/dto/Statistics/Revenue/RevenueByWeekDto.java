package product.management.electronic.dto.Statistics.Revenue;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RevenueByWeekDto {
    private LocalDate week;
    private double totalRevenue;
}
