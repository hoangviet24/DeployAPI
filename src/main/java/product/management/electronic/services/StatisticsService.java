package product.management.electronic.services;

import product.management.electronic.dto.Statistics.Revenue.ProductSalesDto;
import product.management.electronic.dto.Statistics.Revenue.RevenueByDateDto;
import product.management.electronic.dto.Statistics.Summary.RevenueMonthSummaryDto;
import product.management.electronic.dto.Statistics.Summary.RevenueWeekSummaryDto;
import product.management.electronic.dto.Statistics.Summary.RevenueYearSummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {
    List<ProductSalesDto> getTopSellingProducts(LocalDate startDate, LocalDate endDate);
    List<RevenueByDateDto> getRevenueByDate(LocalDate nowDay);
    RevenueWeekSummaryDto getRevenueByWeek(int week);
    RevenueMonthSummaryDto getRevenueByMonth(int month);
    RevenueYearSummaryDto getRevenueByYear(int year);
}
