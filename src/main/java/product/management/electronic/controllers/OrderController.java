package product.management.electronic.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import product.management.electronic.dto.Order.OrderCreateRequestDto;
import product.management.electronic.dto.Order.OrderDto;
import product.management.electronic.dto.Order.UpdateOrderDto;
import product.management.electronic.dto.User.UserDto;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.OrderService;
import product.management.electronic.services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static product.management.electronic.constants.MessageConstant.ORDER_UPDATED_SUCCESS;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderCreateRequestDto orderCreateDto, Authentication authentication) {
        String username = authentication.getName();
        UserDto user = userService.findByUsername(username);
        OrderDto responseDto = orderService.createOrderFromCart(orderCreateDto, user.getId());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Update order status and payment status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody UpdateOrderDto request) {
        OrderDto updatedOrderDto = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(
                new ApiResponse(HttpStatus.OK.value(), ORDER_UPDATED_SUCCESS, updatedOrderDto)
        );
    }
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse> cancelOrderStatus(
            @PathVariable UUID orderId,
            Authentication authentication) {
        String username = authentication.getName();
        UserDto user = userService.findByUsername(username);
        OrderDto updatedOrderDto = orderService.CancelOrderStatus(orderId,user.getId());
        return ResponseEntity.ok(
                new ApiResponse(HttpStatus.OK.value(), ORDER_UPDATED_SUCCESS, updatedOrderDto)
        );
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/getAllOrders")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestParam(defaultValue = "PENDING")OrderStatus orderStatus,
            @RequestParam(defaultValue = "UNPAID") PaymentStatus paymentStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean sort,
            @RequestParam(defaultValue = "createAt") String sortBy
    ) {
        int pageIndex = Math.max(0, page - 1);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), orderService.getAll(orderStatus,paymentStatus,pageIndex, size, sort, sortBy)));
    }

    @Operation(summary = "Get orders by user with optional filters and pagination")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse> getOrdersByUser(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String username = authentication.getName();
        UserDto user = userService.findByUsername(username);
        UUID userId = user.getId();
        Page<OrderDto> orderPage = orderService.getOrdersByUserId(userId, orderStatus, paymentStatus, page, size, sortBy, direction);
        Map<String, Object> response = new HashMap<>();
        response.put("content", orderPage.getContent());
        response.put("totalItems", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        response.put("currentPage", orderPage.getNumber());
        return ResponseEntity.ok(
                new ApiResponse(HttpStatus.OK.value(), "Fetched orders successfully", response)
        );
    }
}
