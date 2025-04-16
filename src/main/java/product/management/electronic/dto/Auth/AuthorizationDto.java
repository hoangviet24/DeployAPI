package product.management.electronic.dto.Auth;
import lombok.Data;
import product.management.electronic.entities.Role;
import java.util.Set;



@Data
public class AuthorizationDto {
    private Token token;
    private UserDto user;

    public AuthorizationDto(String accessToken, String refreshToken, String id, String username, boolean isActive, Set<Role> roles) {
        this.token = new Token(accessToken, refreshToken);
        this.user = new UserDto(id, username, isActive, roles);
    }
    @Data
    public static class Token {
        private String accessToken;
        private String refreshToken;

        public Token(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @Data
    public static class UserDto {
        private String id;
        private String username;
        private boolean isActive;
        private Set<Role> roles;

        public UserDto(String id, String username, boolean isActive, Set<Role> roles) {
            this.id = id;
            this.username = username;
            this.isActive = isActive;
            this.roles = roles;
        }
    }
}