package product.management.electronic.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import product.management.electronic.dto.Order.OrderItemDto;
import product.management.electronic.dto.Statistics.Revenue.*;
import product.management.electronic.dto.Statistics.Summary.RevenueMonthSummaryDto;
import product.management.electronic.dto.Statistics.Summary.RevenueWeekSummaryDto;
import product.management.electronic.dto.Statistics.Summary.RevenueYearSummaryDto;
import product.management.electronic.entities.Order;
import product.management.electronic.entities.OrderItem;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;
import product.management.electronic.mapper.OrderMapper;
import product.management.electronic.mapper.StatisticsMapper;
import product.management.electronic.repository.StatisticsRepository;
import product.management.electronic.services.StatisticsService;

import java.time.*;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final OrderMapper orderMapper;
    private final StatisticsRepository statisticsRepository;
    private final StatisticsMapper statisticsMapper;

    @Override
    public List<ProductSalesDto> getTopSellingProducts(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = statisticsRepository.findByCreatedAtBetweenAndOrderStatusAndPaymentStatus(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                OrderStatus.COMPLETED,
                PaymentStatus.PAID
        );

        Map<String, ProductSalesDto> productSalesMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderDetails()) {
                OrderItemDto itemDto = orderMapper.mapToDto(item);
                String productIdStr = item.getProduct().getId().toString();
                productSalesMap.compute(productIdStr, (id, existing) -> {
                    if (existing == null) {
                        return new ProductSalesDto(
                                item.getProduct().getId(),
                                itemDto.getProductName(),
                                itemDto.getThumbnail(),
                                itemDto.getQuantity(),
                                itemDto.getTotalPrice()
                        );

                    } else {
                        existing.setQuantitySold(existing.getQuantitySold() + itemDto.getQuantity());
                        existing.setTotalRevenue(existing.getTotalRevenue() + itemDto.getTotalPrice());

                        return existing;
                    }
                });
            }
        }
        return productSalesMap.values().stream()
                .sorted(Comparator.comparing(ProductSalesDto::getQuantitySold).reversed())
                .toList();
    }

    @Override
    public List<RevenueByDateDto> getRevenueByDate(LocalDate nowDay) {
        LocalDateTime startDate = nowDay.atStartOfDay();
        LocalDateTime endDate = nowDay.atTime(LocalTime.MAX);
        List<Order> orders = statisticsRepository.findByCreatedAtBetweenAndOrderStatusAndPaymentStatus(
                startDate,
                endDate,
                OrderStatus.COMPLETED,
                PaymentStatus.PAID
        );
        Map<LocalDate,Double> revenueMap = new TreeMap<>();
        for(Order order: orders){
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            double totalAmount = order.getOrderDetails()
                    .stream()
                    .mapToDouble(OrderItem::getTotalPrice)
                    .sum();

            revenueMap.put(orderDate,
                    revenueMap.getOrDefault(orderDate, 0.0) + totalAmount);
        }
        return revenueMap.entrySet().stream()
                .map(entry -> statisticsMapper.toRevenueByDateDto(entry.getKey(),entry.getValue()))
                .toList();
    }

    @Override
    public RevenueWeekSummaryDto getRevenueByWeek(int week) {
        int currentYear = Year.now().getValue();

        LocalDate startDate = LocalDate
                .now()
                .withYear(currentYear)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(DayOfWeek.MONDAY);

        LocalDate endDate = startDate.plusDays(6);

        List<Order> orders = statisticsRepository.findByCreatedAtBetweenAndOrderStatusAndPaymentStatus(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                OrderStatus.COMPLETED,
                PaymentStatus.PAID
        );

        Map<LocalDate, Double> revenueMap = new TreeMap<>();
        for (Order order : orders) {
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            double totalAmount = order.getOrderDetails()
                    .stream()
                    .mapToDouble(OrderItem::getTotalPrice)
                    .sum();
            revenueMap.merge(orderDate, totalAmount, Double::sum);
        }

        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            revenueMap.putIfAbsent(day, 0.0);
        }

        List<RevenueByWeekDto> dailyRevenue = revenueMap.entrySet().stream()
                .map(entry -> statisticsMapper.toRevenueByWeekDto(entry.getKey(), entry.getValue()))
                .toList();

        double weeklyTotal = dailyRevenue.stream()
                .mapToDouble(RevenueByWeekDto::getTotalRevenue)
                .sum();

        return new RevenueWeekSummaryDto(dailyRevenue, weeklyTotal);
    }

    @Override
    public RevenueMonthSummaryDto getRevenueByMonth(int month) {
        int currentYear = Year.now().getValue();
        LocalDate startDate = LocalDate.of(currentYear, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Order> orders = statisticsRepository.findByCreatedAtBetweenAndOrderStatusAndPaymentStatus(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                OrderStatus.COMPLETED,
                PaymentStatus.PAID
        );
        List<LocalDate> weekStartDates = getCustomWeekStartDates(startDate, endDate);
        Map<LocalDate, Double> weekRevenueMap = new LinkedHashMap<>();

        for (int i = 0; i < weekStartDates.size(); i++) {
            LocalDate weekStart = weekStartDates.get(i);
            LocalDate weekEnd = (i + 1 < weekStartDates.size())
                    ? weekStartDates.get(i + 1).minusDays(1)
                    : endDate;

            double weeklyRevenue = orders.stream()
                    .filter(order -> {
                        LocalDate orderDate = order.getCreatedAt().toLocalDate();
                        return !orderDate.isBefore(weekStart) && !orderDate.isAfter(weekEnd);
                    })
                    .mapToDouble(order -> order.getOrderDetails()
                            .stream()
                            .mapToDouble(OrderItem::getTotalPrice)
                            .sum())
                    .sum();

            weekRevenueMap.put(weekStart, weeklyRevenue);
        }

        double totalMonthRevenue = weekRevenueMap.values().stream().mapToDouble(Double::doubleValue).sum();

        List<RevenueByWeekDto> weeklyRevenue = weekRevenueMap.entrySet().stream()
                .map(entry -> new RevenueByWeekDto(entry.getKey(), entry.getValue()))
                .toList();

        RevenueByMonthDto monthDto = new RevenueByMonthDto();
        monthDto.setMonth(startDate);
        monthDto.setTotalRevenue(totalMonthRevenue);

        return new RevenueMonthSummaryDto(weeklyRevenue, monthDto);
    }
    private List<LocalDate> getCustomWeekStartDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> weekStartDates = new ArrayList<>();
        weekStartDates.add(startDate);

        LocalDate current = startDate;

        while (!current.getDayOfWeek().equals(DayOfWeek.SUNDAY) && !current.isAfter(endDate)) {
            current = current.plusDays(1);
        }

        current = current.plusDays(1);

        while (!current.isAfter(endDate)) {
            weekStartDates.add(current);
            current = current.plusWeeks(1);
        }

        return weekStartDates;
    }

    @Override
    public RevenueYearSummaryDto getRevenueByYear(int year) {
        List<RevenueByYearDto> revenueList = new ArrayList<>();
        double yearlyTotal = 0;

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            List<Order> orders = statisticsRepository.findByCreatedAtBetweenAndOrderStatusAndPaymentStatus(
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX),
                    OrderStatus.COMPLETED,
                    PaymentStatus.PAID
            );

            double totalAmount = orders.stream()
                    .mapToDouble(order -> order.getOrderDetails().stream()
                            .mapToDouble(OrderItem::getTotalPrice)
                            .sum())
                    .sum();

            revenueList.add(new RevenueByYearDto(month, totalAmount));
            yearlyTotal += totalAmount;
        }
        return new RevenueYearSummaryDto(revenueList, yearlyTotal);
    }
}