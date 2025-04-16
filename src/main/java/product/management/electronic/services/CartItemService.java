package product.management.electronic.services;

import product.management.electronic.entities.CartItem;
import product.management.electronic.entities.Order;
import product.management.electronic.entities.OrderItem;

import java.util.List;
import java.util.UUID;

public interface CartItemService {
    List<CartItem> findUserById(UUID id);
    void deleteAll(List<CartItem> cartItems );
    List<CartItem> findAllByIdIn(List<UUID> id);
}
