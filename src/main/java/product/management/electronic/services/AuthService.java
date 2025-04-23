package product.management.electronic.services;

import jakarta.mail.MessagingException;
import product.management.electronic.dto.Auth.AuthDto;
import product.management.electronic.dto.Auth.AuthenticationDto;
import product.management.electronic.dto.Auth.LoginDto;
import product.management.electronic.dto.Auth.RegisterDto;
import product.management.electronic.response.ApiResponse;

import java.io.IOException;

public interface AuthService {
    ApiResponse<LoginDto> login(AuthenticationDto authenticationDto);

    void logout(String authorizationHeader);

    ApiResponse<LoginDto> loginGoogle(AuthenticationDto authenticationDto);
    AuthDto registerUser(LoginDto request) throws MessagingException, IOException;
    ApiResponse<LoginDto> authGoogle(AuthenticationDto authenticationDto) throws MessagingException, IOException;
}
