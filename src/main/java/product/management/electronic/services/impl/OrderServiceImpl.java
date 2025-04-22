package product.management.electronic.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import product.management.electronic.dto.Order.OrderCreateRequestDto;
import product.management.electronic.dto.Order.OrderDto;
import product.management.electronic.dto.Order.UpdateOrderDto;
import product.management.electronic.entities.*;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.mapper.OrderMapper;
import product.management.electronic.repository.OrderRepository;
import product.management.electronic.services.CartItemService;
import product.management.electronic.services.CartService;
import product.management.electronic.services.OrderService;
import product.management.electronic.services.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static product.management.electronic.constants.MessageConstant.*;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {
    private final CartItemService cartItemService;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductService productService;
    @Override
    public OrderDto createOrderFromCart(OrderCreateRequestDto dto, UUID userId) {
        Cart cart = cartService.findUser(userId);
        if (cart == null) throw new ResourceNotFoundException(CART_NOT_FOUND + userId);
        List<CartItem> cartItems = cartItemService.findAllByIdIn(dto.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty!");
        }
        Order order = new Order();
        order.setUser(cart.getUsers());
        order.setShippingAddress(dto.getShippingAddress());
        order.setNote(dto.getNote());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProduct().getQuantity() < cartItem.getQuantity()) {
                throw new ResourceNotFoundException(PRODUCT_NOT_ENOUGH + cartItem.getProduct().getName());
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setThumbnail(cartItem.getThumbnail());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productService.saveProduct(product);
        }
        order.setOrderDetails(orderItems);
        orderRepository.save(order);
        cartItemService.deleteAll(cartItems);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto updateOrderStatus(UUID orderId, UpdateOrderDto request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND));
        OrderStatus newOrderStatus = request.getOrderStatus();
        PaymentStatus newPaymentStatus = request.getPaymentStatus();
        if (newOrderStatus != null) {
            order.setOrderStatus(newOrderStatus);
        }
        if (newPaymentStatus != null) {
            order.setPaymentStatus(newPaymentStatus);
        }
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDto(updatedOrder);
    }

    @Override
    public List<OrderDto> getAll(OrderStatus orderStatus, PaymentStatus paymentStatus, int page, int size, boolean sort, String sortBy) {
        Pageable pageable;
        if (sort) {
            Sort sorting = Sort.by(sortBy).descending();
            pageable = PageRequest.of(page, size, sorting);
        } else {
            pageable = PageRequest.of(page, size);
        }
        List<Order> orderPage = orderRepository.findAllByOrderStatusAndPaymentStatus(orderStatus, paymentStatus, pageable);
        return orderMapper.todtoList(orderPage);
    }

    @Override
    public Page<OrderDto> getOrdersByUserId(UUID userId, OrderStatus orderStatus, PaymentStatus paymentStatus, int page, int size, String sortBy, String direction) {
        String validatedSortBy = validateSortProperty(sortBy);
        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(validatedSortBy).ascending() :
                Sort.by(validatedSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> ordersPage = orderRepository.findAllByUserId(userId, pageable);
        if (orderStatus == null && paymentStatus == null) {
            return ordersPage.map(orderMapper::toDto);
        }

        List<Order> filteredOrders = ordersPage.getContent().stream()
                .filter(order -> orderStatus == null || order.getOrderStatus() == orderStatus)
                .filter(order -> paymentStatus == null || order.getPaymentStatus() == paymentStatus)
                .collect(Collectors.toList());

        List<OrderDto> orderDtos = filteredOrders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(orderDtos, pageable, orderDtos.size());
    }

    private String validateSortProperty(String sortBy) {
        List<String> validProperties = List.of("createdAt", "orderStatus", "paymentStatus", "id");
        return validProperties.contains(sortBy) ? sortBy : "createdAt";
    }

    public Optional<Order> findByNoteAndCalculatedTotalAndPaymentStatus(String note, double totalAmount, PaymentStatus paymentStatus) {
        List<Order> orders = orderRepository.findByNoteAndPaymentStatus(note, paymentStatus);

        return orders.stream()
                .filter(order -> {
                    double calculatedTotal = calculateTotalAmount(order);
                    return Math.abs(calculatedTotal - totalAmount) < 0.001;
                })
                .findFirst();
    }

    public double calculateTotalAmount(Order order) {
        return order.getOrderDetails().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }
}