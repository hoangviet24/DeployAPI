package product.management.electronic.dto.Statistics.Summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import product.management.electronic.dto.Statistics.Revenue.RevenueByYearDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueYearSummaryDto {
    private List<RevenueByYearDto> monthlyRevenue;
    private double yearlyTotal;
}
