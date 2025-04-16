package product.management.electronic.services;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import product.management.electronic.dto.Auth.AuthDto;
import product.management.electronic.dto.Auth.RegisterDto;
import product.management.electronic.dto.User.UpdateUserDto;
import product.management.electronic.entities.Role;
import product.management.electronic.entities.User;
import product.management.electronic.enums.RoleType;
import product.management.electronic.exceptions.BadRequestException;
import product.management.electronic.exceptions.ConflictException;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.mapper.UserMapper;
import product.management.electronic.repository.UserRepository;
import product.management.electronic.services.impl.UserServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private MimeMessage mimeMessage;
    RegisterDto validRequest;
    User user;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterDto("username", "fullname", "email@example.com",
                "password", "phone", "address", Collections.singleton("ROLE_ADMIN"));
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(validRequest.getUsername());
        user.setEmail(validRequest.getEmail());
        user.setPassword("encodedPassword");
        user.setActive(false);
    }

    //Register
    @Test
    void testRegisterUser_WhenValid_Success() throws Exception {
        when(userMapper.toEntity(validRequest)).thenReturn(user);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(mimeMessage);

        AuthDto authDto = userService.registerUser(validRequest);

        assertEquals(user.getEmail(), authDto.getEmail());
        verify(userRepository).save(any(User.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    public void testRegisterUser_ConflictUsername() {
        when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);
        assertThrows(ConflictException.class, () -> userService.registerUser(validRequest));
    }

    @Test
    public void testRegisterUser_ConflictEmail() {
        when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);
        assertThrows(ConflictException.class, () -> userService.registerUser(validRequest));
    }
    //Active
    @Test
    public void testActivateAccount_WithValidToken() {
        String token = "validToken";
        User user = new User();
        user.setActivationToken(token);
        user.setActive(false);

        when(userRepository.findByActivationToken(token)).thenReturn(Optional.of(user));

        userService.activateAccount(token);

        assertTrue(user.isActive());
        assertNull(user.getActivationToken());

        verify(userRepository).findByActivationToken(token);
        verify(userRepository).save(user);
    }
    @Test
    public void testActivateAccount_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalidToken";

        when(userRepository.findByActivationToken(invalidToken)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.activateAccount(invalidToken));
        verify(userRepository).findByActivationToken(invalidToken);
    }
    //Change password
    @Test
    public void testChangePassword() {
        String username = "testuser";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(oldPassword));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        AuthDto result = userService.changePassword(username, oldPassword, newPassword);

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).findByUsername(username);
        verify(userRepository).save(user);
    }
    @Test
    public void testChangePassword_NewPasswordIsNull() {
        String username = "testuser";
        String oldPassword = "oldPassword";

        Exception exception = assertThrows(BadRequestException.class, () ->
                userService.changePassword(username, oldPassword, null));

        assertEquals("New password cannot be null!", exception.getMessage());
    }
    @Test
    public void testChangePassword_WrongOldPassword() {
        String username = "testuser";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("anotherPassword")); // mật khẩu khác

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(BadRequestException.class, () ->
                userService.changePassword(username, oldPassword, newPassword));

        assertEquals("Old password is not correct!", exception.getMessage());
    }
    //forgotPassword
    @Test
    public void testForgotPassword_Success() throws Exception{
        String email = "email@example.com";
        String username = "testuser";
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        userService.forgotPassword(email);

        verify(userRepository).findByEmail(email);
        verify(mailSender).send(mimeMessage);
    }
    @Test
    public void testForgotPassword_EmailNotFound() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.forgotPassword(email));

        assertEquals("Email not found!", exception.getMessage());
    }
    @Test
    public void testForgotPassword_EmailSendFails() throws Exception {
        String email = "user@example.com";
        String username = "testuser";
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Simulated email failure")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> userService.forgotPassword(email));
    }
    //load user
    @Test
    void testLoadUserByUsername_WhenUserExists() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        Role role = new Role();
        role.setName(RoleType.ROLE_USER);
        user.setRole(Set.of(role));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertEquals("testuser", userDetails.getUsername());
        assertEquals("hashedpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_WhenUserNotFound() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.loadUserByUsername("notfound"));
    }
    //get User by Refresh token
    @Test
    void testGetUserByRefreshToken_WhenTokenExists() {
        User user = new User();
        when(userRepository.findByRefreshToken("valid-token")).thenReturn(Optional.of(user));

        User result = userService.getUserByRefreshToken("valid-token");

        assertEquals(user, result);
    }

    @Test
    void testGetUserByRefreshToken_WhenTokenInvalid() {
        when(userRepository.findByRefreshToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.getUserByRefreshToken("invalid-token"));
    }
    //Update user
    @Test
    void testUpdateUserById_WhenEmailAlreadyExists() {
        UUID id = UUID.randomUUID();
        UpdateUserDto request = new UpdateUserDto("newname", "test@example.com", "0123456789", "new address");
        when(userRepository.findUserById(id)).thenReturn(Optional.of(new User()));
        when(userRepository.existsByEmailAndIdNot("test@example.com", id)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUserById(id.toString(), request));
    }

    @Test
    void testUpdateUserById_Success() {
        UUID id = UUID.randomUUID();
        UpdateUserDto request = new UpdateUserDto("newname", "test@example.com", "0123456789", "new address");

        User user = new User();
        user.setId(id);
        when(userRepository.findUserById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("test@example.com", id)).thenReturn(false);

        userService.updateUserById(id.toString(), request);

        assertEquals("newname", user.getFullname());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("0123456789", user.getPhone());
        assertEquals("new address", user.getAddress());
        verify(userRepository).save(user);
    }
}