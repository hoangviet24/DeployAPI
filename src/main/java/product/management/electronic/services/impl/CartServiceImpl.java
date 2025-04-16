package product.management.electronic.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import product.management.electronic.dto.Cart.CartDto;
import product.management.electronic.dto.Cart.CartItemAddDto;
import product.management.electronic.dto.Cart.UpdateCartDto;
import product.management.electronic.entities.Cart;
import product.management.electronic.entities.CartItem;
import product.management.electronic.entities.Product;
import product.management.electronic.entities.User;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.mapper.CartMapper;
import product.management.electronic.repository.CartRepository;
import product.management.electronic.services.CartService;
import product.management.electronic.services.ProductService;
import product.management.electronic.services.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static product.management.electronic.constants.MessageConstant.*;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final UserService userService;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;

    @Transactional
    @Override
    public CartDto addToCart(CartItemAddDto request) {
        User user = userService.getUserId(request.getUserId());
        Cart cart = cartRepository.findByUsers(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUsers(user);
            return cartRepository.save(newCart);
        });
        Optional<CartItem> existingItem = cart.getItems().stream().filter(item -> item.getProduct().getId().equals(request.getProductId())).findFirst();
        Product product = productService.getProductById(request.getProductId());
        if (product.getQuantity() < request.getQuantity()) {
            throw new ResourceNotFoundException(PRODUCT_NOT_ENOUGH);
        }

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setTotalPrice(cartItem.getQuantity() * cartItem.getUnitPrice());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setNameProduct(product.getName());
            newItem.setThumbnail(product.getThumbnail());
            newItem.setQuantity(request.getQuantity());
            BigDecimal unitPrice = product.getPrice();
            BigDecimal quantity = new BigDecimal(request.getQuantity());
            BigDecimal totalPriceBigDecimal = unitPrice.multiply(quantity);
            double totalPrice = totalPriceBigDecimal.doubleValue();
            newItem.setUnitPrice(unitPrice.doubleValue());
            newItem.setTotalPrice(totalPrice);
            cart.getItems().add(newItem);
        }
        return cartMapper.toCartDTO(cartRepository.save(cart));
    }

    public CartDto updateCart(UUID cartId, List<UpdateCartDto> cartItemRequests) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException(CART_NOT_FOUND + cartId));

        Map<UUID, UpdateCartDto> requestsByProductId = cartItemRequests.stream()
                .collect(Collectors.toMap(UpdateCartDto::getProductId, item -> item));
        List<UUID> productIds = cartItemRequests.stream()
                .map(UpdateCartDto::getProductId)
                .collect(Collectors.toList());
        Map<UUID, Product> productsMap = productService.getAll(productIds);
        Map<UUID, CartItem> existingItemsByProductId = cart.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));
        for (UpdateCartDto request : cartItemRequests) {
            UUID productId = request.getProductId();
            Product product = productsMap.get(productId);

            if (product == null) {
                throw new ResourceNotFoundException(ITEM_NOT_FOUND + productId);
            }
            CartItem cartItem = existingItemsByProductId.get(productId);

            if (cartItem != null) {
                if (request.getQuantity() <= 0) {
                    cart.getItems().remove(cartItem);
                } else {
                    cartItem.setQuantity(request.getQuantity());
                    cartItem.setUnitPrice(product.getPrice().doubleValue());
                    cartItem.setTotalPrice(product.getPrice().multiply(new BigDecimal(request.getQuantity())).doubleValue());
                }
            } else if (request.getQuantity() > 0) {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setNameProduct(product.getName());
                newItem.setThumbnail(product.getThumbnail());
                newItem.setQuantity(request.getQuantity());
                newItem.setUnitPrice(product.getPrice().doubleValue());
                newItem.setTotalPrice(product.getPrice().multiply(new BigDecimal(request.getQuantity())).doubleValue());

                cart.getItems().add(newItem);
            }
        }
        Cart updatedCart = cartRepository.save(cart);
        return cartMapper.toCartDTO(updatedCart);
    }
  
   @Override
    public CartDto getCartByUserId(UUID userId) {
       Cart cart = cartRepository.findByUsers(userService.getUserId(userId)).orElseThrow(() -> new ResourceNotFoundException(CART_NOT_FOUND));
        return cartMapper.toCartDTO(cart);
    }

    @Override
    public Cart findUser(UUID id) {
        return cartRepository.findByUsers(userService.getUserId(id)).orElseThrow(() -> new ResourceNotFoundException(USER_NOTFOUND));
    }
}