package product.management.electronic.dto.Statistics.Revenue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueByMonthDto {
    private LocalDate month;
    private double totalRevenue;
}
