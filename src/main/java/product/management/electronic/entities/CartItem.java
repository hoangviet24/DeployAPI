package product.management.electronic.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;
import java.util.UUID;

@Entity(name = "cart_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, updatable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    private String nameProduct;
    private String thumbnail;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}