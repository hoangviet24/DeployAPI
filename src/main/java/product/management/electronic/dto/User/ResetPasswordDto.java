package product.management.electronic.dto.User;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String username;
    private String oldPassword;
    private String newPassword;
}
