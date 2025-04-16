package product.management.electronic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import product.management.electronic.entities.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByEmail(String email);
    Optional<User> findByActivationToken(String token);
    @Query("SELECT us FROM User us WHERE us.id = ?1")
    Optional<User> findUserById(UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
}
