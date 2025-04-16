package product.management.electronic.dto.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private UUID productId;
    private String productName;
    private String thumbnail;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}
