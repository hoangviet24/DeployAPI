package product.management.electronic.dto.Statistics.Summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import product.management.electronic.dto.Statistics.Revenue.RevenueByWeekDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueWeekSummaryDto {
    private List<RevenueByWeekDto> dailyRevenue;
    private double weeklyTotal;
}
