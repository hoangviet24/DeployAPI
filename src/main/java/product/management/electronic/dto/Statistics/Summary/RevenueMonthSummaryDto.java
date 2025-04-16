package product.management.electronic.dto.Statistics.Summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import product.management.electronic.dto.Statistics.Revenue.RevenueByMonthDto;
import product.management.electronic.dto.Statistics.Revenue.RevenueByWeekDto;

import java.util.List;

@Data
@AllArgsConstructor
public class RevenueMonthSummaryDto {
    private List<RevenueByWeekDto> weeklyRevenue;
    private RevenueByMonthDto totalMonthRevenue;
}
