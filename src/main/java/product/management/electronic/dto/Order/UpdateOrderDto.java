package product.management.electronic.dto.Order;

import lombok.Data;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;

@Data
public class UpdateOrderDto {
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
}
