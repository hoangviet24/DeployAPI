package product.management.electronic.dto.Cart;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {
    private UUID id;
    private UUID userId;
    private List<CartItemDto> cartDetails;
    private double totalPrice;
}