package product.management.electronic.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, updatable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String type;
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;

}

