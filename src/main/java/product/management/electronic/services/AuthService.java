package product.management.electronic.services;

import jakarta.mail.MessagingException;
import product.management.electronic.dto.Auth.*;
import product.management.electronic.response.ApiResponse;

import java.io.IOException;

public interface AuthService {
    ApiResponse<LoginDto> login(AuthenticationDto authenticationDto);

    void logout(String authorizationHeader);

    AuthDto registerUser(LoginDto request) throws MessagingException, IOException;
    ApiResponse<LoginDto> authGoogle(LoginGoogleDto loginGoogleDto) throws MessagingException, IOException;
}
