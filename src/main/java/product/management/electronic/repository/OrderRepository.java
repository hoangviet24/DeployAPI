package product.management.electronic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import product.management.electronic.entities.Order;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByOrderStatusAndPaymentStatus(OrderStatus orderStatus, PaymentStatus paymentStatus, Pageable pageable);
    @Query("SELECT o FROM orders o WHERE o.user.id = :userId")
    Page<Order> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
    List<Order> findByNoteAndPaymentStatus(String note, PaymentStatus paymentStatus);

}
