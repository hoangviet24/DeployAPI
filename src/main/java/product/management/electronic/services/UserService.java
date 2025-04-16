package product.management.electronic.services;

import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetails;
import product.management.electronic.dto.Auth.AuthDto;
import product.management.electronic.dto.Auth.RegisterDto;
import product.management.electronic.dto.User.UpdateUserDto;
import product.management.electronic.dto.User.UserDto;
import product.management.electronic.entities.User;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDetails loadUserByUsername(String username);

    UserDto findByUsername(String username);

    User getUserByRefreshToken(String refreshToken);

    void save(User user);

    AuthDto registerUser(RegisterDto request) throws MessagingException, IOException;

    AuthDto changePassword(String username, String oldPassword, String newPassword);

    void forgotPassword(String email) throws MessagingException, IOException;

    void sendEmailResetPassword(String to, String subject, String content, String username)
            throws MessagingException, IOException;

    String generateRandomPassword();

    void sendEmailActivation(String to, String username, String token)
            throws MessagingException, IOException;

    void activateAccount(String token);

    List<UserDto> getAllUsers();

    UserDto getUserById(UUID id);
    User getUserId(UUID id);
    UserDto updateUserById(String id, UpdateUserDto request);
}