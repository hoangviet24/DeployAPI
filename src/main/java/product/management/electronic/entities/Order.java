package product.management.electronic.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import product.management.electronic.enums.OrderStatus;
import product.management.electronic.enums.PaymentStatus;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, updatable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "userId",unique = false)
    private User user;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'UNPAID'")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'UNPAID'")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderDetails = new ArrayList<>();
    private String note;
    private String shippingAddress;
}