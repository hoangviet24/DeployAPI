package product.management.electronic.dto.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
    private String username;
    private String fullname;
    private String email;
    private String password;
    private String phone;
    private String address;
    private Set<String> roles;
}
