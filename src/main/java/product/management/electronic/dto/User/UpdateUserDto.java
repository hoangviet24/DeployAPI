package product.management.electronic.dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto {
    private String fullname;
    private String email;
    private String phone;
    private String address;
}
