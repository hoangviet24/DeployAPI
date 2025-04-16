package product.management.electronic.dto.Auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginDto {
    private UUID id;
    private String username;
    private String email;
    private String accessToken;
    private String refreshToken;
}
