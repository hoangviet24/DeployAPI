package product.management.electronic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import product.management.electronic.entities.Order;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StatisticsRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCreatedAtBetweenAndOrderStatusAndPaymentStatus(
            LocalDateTime start,
            LocalDateTime end,
            OrderStatus status,
            PaymentStatus paymentStatus
    );

}
