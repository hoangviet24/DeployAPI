package product.management.electronic.dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserDto {
    private UUID id;
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String address;
    private Set<String> roles;
    private boolean isActive;
    private LocalDateTime createAt;
}
