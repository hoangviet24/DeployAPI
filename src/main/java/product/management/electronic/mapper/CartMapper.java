package product.management.electronic.mapper;

import org.springframework.stereotype.Component;
import product.management.electronic.dto.Cart.CartDto;
import product.management.electronic.dto.Cart.CartItemDto;
import product.management.electronic.entities.Cart;
import product.management.electronic.entities.CartItem;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {
    public CartItemDto toCartItemDTO(CartItem item) {
        return new CartItemDto(
                item.getId(),
                item.getCart().getId(),
                item.getProduct().getId(),
                item.getNameProduct(),
                item.getThumbnail(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }

    public CartDto toCartDTO(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());

        double totalPrice = items.stream()
                .mapToDouble(CartItemDto::getTotalPrice)
                .sum();
        return new CartDto(cart.getId(), cart.getUsers().getId(), items, totalPrice);
    }
}