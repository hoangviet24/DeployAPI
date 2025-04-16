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
public class CartItemDto {
    private UUID id;
    private UUID cart;
    private UUID productId;
    private String nameProduct;
    private String thumbnail;
    private int quantity;
    private Double unitPrice;
    private Double totalPrice;
}