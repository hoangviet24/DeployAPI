package product.management.electronic.dto.Auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDto {
    private UUID id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}