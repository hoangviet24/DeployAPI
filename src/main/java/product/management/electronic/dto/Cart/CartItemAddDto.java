package product.management.electronic.dto.Cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemAddDto {
    private UUID userId;
    private UUID productId;
    private int quantity;
}
