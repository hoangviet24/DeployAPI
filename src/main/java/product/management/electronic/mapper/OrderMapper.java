package product.management.electronic.mapper;

import org.springframework.stereotype.Component;
import product.management.electronic.dto.Order.OrderItemDto;
import product.management.electronic.dto.Order.OrderDto;
import product.management.electronic.entities.Order;
import product.management.electronic.entities.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getId());
        dto.setUserEmail(order.getUser().getEmail());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setNote(order.getNote());

        List<OrderItemDto> itemDtos = order.getOrderDetails().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);
        dto.setTotalAmount(calculateTotalAmount(itemDtos));
        return dto;
    }
    public List<OrderDto> todtoList(List<Order> orders){
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }
    private OrderItemDto toItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProductName());
        dto.setThumbnail(item.getThumbnail());
        dto.setQuantity(item.getQuantity());

        double unitPrice = item.getUnitPrice();
        double totalPrice = unitPrice * item.getQuantity();

        dto.setUnitPrice(unitPrice);
        dto.setTotalPrice(totalPrice);
        return dto;
    }

    private double calculateTotalAmount(List<OrderItemDto> items) {
        return items.stream()
                .mapToDouble(OrderItemDto::getTotalPrice)
                .sum();
    }
    public OrderItemDto mapToDto(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getProductName(),
                item.getThumbnail(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}
