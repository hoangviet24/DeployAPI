package product.management.electronic.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import product.management.electronic.dto.Order.OrderCreateRequestDto;
import product.management.electronic.dto.Order.OrderDto;
import product.management.electronic.dto.Order.UpdateOrderDto;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    public OrderDto createOrderFromCart(OrderCreateRequestDto dto, UUID userId);
    OrderDto updateOrderStatus(UUID orderId, UpdateOrderDto request);

    List<OrderDto> getAll(OrderStatus orderStatus, PaymentStatus paymentStatus, int page, int size, boolean sort, String sortBy);
    Page<OrderDto> getOrdersByUserId(UUID userId, OrderStatus orderStatus, PaymentStatus paymentStatus, int page, int size, String sortBy, String direction);

}
