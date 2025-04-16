package product.management.electronic.services;

import product.management.electronic.dto.Auth.AuthenticationDto;
import product.management.electronic.dto.Auth.LoginDto;
import product.management.electronic.response.ApiResponse;
public interface AuthService {
    ApiResponse<LoginDto> login(AuthenticationDto authenticationDto);

    void logout(String authorizationHeader);

}
