package product.management.electronic.services;

import product.management.electronic.dto.Cart.CartDto;
import product.management.electronic.dto.Cart.CartItemAddDto;
import product.management.electronic.entities.Cart;
import product.management.electronic.entities.User;
import product.management.electronic.dto.Cart.UpdateCartDto;

import java.util.List;
import java.util.UUID;

public interface CartService {
    CartDto updateCart(UUID cartId, List<UpdateCartDto> cartItemRequests);
    public CartDto addToCart(CartItemAddDto request);
    public CartDto getCartByUserId(UUID userId);
    Cart findUser(UUID id);
    void removeFromCart(UUID uuid, UUID productId);
}