package product.management.electronic.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import product.management.electronic.entities.CartItem;
import product.management.electronic.entities.OrderItem;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.repository.CartItemRepository;
import product.management.electronic.services.CartItemService;

import java.util.List;
import java.util.UUID;

import static product.management.electronic.constants.MessageConstant.ITEM_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    @Override
    public List<CartItem> findUserById(UUID id) {
        return cartItemRepository.findUserById(id);
    }

    @Override
    public void deleteAll(List<CartItem> cartItems) {
        cartItemRepository.deleteAll(cartItems);
    }

    @Override
    public List<CartItem> findAllByIdIn(List<UUID> id) {
        return cartItemRepository.findAllByIdIn(id);
    }

    @Override
    public void deleteCartItemById(UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND + cartItemId));
        cartItemRepository.delete(cartItem);
    }
}
