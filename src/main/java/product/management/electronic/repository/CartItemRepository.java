package product.management.electronic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import product.management.electronic.entities.Cart;
import product.management.electronic.entities.CartItem;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findUserById(UUID id);
    List<CartItem> findAllByIdIn(List<UUID> id);
}
