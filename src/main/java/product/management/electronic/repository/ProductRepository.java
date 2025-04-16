package product.management.electronic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import product.management.electronic.entities.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<List<Product>> findByNameContaining(String name, Pageable pageable);

    Page<Product> findAll(Pageable pageable);
}