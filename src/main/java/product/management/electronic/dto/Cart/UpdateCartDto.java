package product.management.electronic.dto.Cart;

import lombok.Data;
import java.util.UUID;

@Data
public class UpdateCartDto {
    private UUID userId;
    private UUID productId;
    private int quantity;
}