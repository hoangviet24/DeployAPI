package product.management.electronic.mapper;

import org.springframework.stereotype.Component;
import product.management.electronic.dto.Statistics.Revenue.RevenueByDateDto;
import product.management.electronic.dto.Statistics.Revenue.RevenueByMonthDto;
import product.management.electronic.dto.Statistics.Revenue.RevenueByWeekDto;

import java.time.LocalDate;

@Component
public class StatisticsMapper {
    public RevenueByDateDto toRevenueByDateDto(LocalDate date, double totalRevenue){
        return new RevenueByDateDto(date,totalRevenue);
    }
    public RevenueByWeekDto toRevenueByWeekDto(LocalDate date, double totalRevenue){
        return new RevenueByWeekDto(date,totalRevenue);
    }
    public RevenueByMonthDto toRevenueByMonthDto(LocalDate date, double totalRevenue){
        return new RevenueByMonthDto(date,totalRevenue);
    }
}
