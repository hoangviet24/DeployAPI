package product.management.electronic.dto.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private UUID orderId;
    private String userEmail;
    private LocalDateTime createdAt;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String shippingAddress;
    private String note;
    private double totalAmount;
    private List<OrderItemDto> items;
}
