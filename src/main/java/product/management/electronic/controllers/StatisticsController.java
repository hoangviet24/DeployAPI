package product.management.electronic.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import product.management.electronic.dto.Statistics.Revenue.ProductSalesDto;
import product.management.electronic.dto.Statistics.Revenue.RevenueByDateDto;
import product.management.electronic.dto.Statistics.Summary.RevenueWeekSummaryDto;
import product.management.electronic.enums.Period;
import product.management.electronic.exceptions.MethodArgumentTypeMismatchException;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.StatisticsService;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse> getTopSellingProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }
        List<ProductSalesDto> productSalesDto = statisticsService.getTopSellingProducts(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), productSalesDto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/revenue-by-date")
    public ResponseEntity<ApiResponse> getRevenueByDate(
           @RequestParam(required = false) LocalDate date
    ) {
        try{
            if(date == null){
                date = LocalDate.now();
            }
            List<RevenueByDateDto> revenue = statisticsService.getRevenueByDate(date);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), revenue));
        }
        catch (Exception e){
            throw new MethodArgumentTypeMismatchException(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/revenue-by-period")
    public ResponseEntity<ApiResponse> getRevenueByPeriod(
            @RequestParam(required = false,defaultValue = "WEEK") Period period,
            @RequestParam(required = false) Integer value
    ){
        switch (period) {
            case WEEK -> {
                int week = (value != null) ? value : LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), statisticsService.getRevenueByWeek(week)));
            }
            case MONTH -> {
                int month = (value != null) ? value : LocalDate.now().getMonthValue();
                return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), statisticsService.getRevenueByMonth(month)));
            }
            case YEAR -> {
                int year = (value != null) ? value : LocalDate.now().getYear();
                return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), statisticsService.getRevenueByYear(year)));
            }
            default -> throw new IllegalArgumentException("Invalid period");
        }

    }
}