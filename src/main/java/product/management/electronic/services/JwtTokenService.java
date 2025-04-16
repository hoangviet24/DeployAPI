package product.management.electronic.services;

import org.springframework.security.core.userdetails.UserDetails;
public interface JwtTokenService {
    String createToken(String userName);

    String extractUserNameFromJWT(String token);

    String createRefreshToken(String token);

    boolean verifyExpiration(String authToken);

    boolean isValidToken(String token, UserDetails userDetails);
}
