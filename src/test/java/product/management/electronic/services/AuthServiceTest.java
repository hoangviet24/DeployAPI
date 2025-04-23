package product.management.electronic.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static product.management.electronic.constants.MessageConstant.TOKEN_INVALID;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import product.management.electronic.dto.Auth.AuthenticationDto;
import product.management.electronic.dto.Auth.LoginDto;
import product.management.electronic.entities.User;
import product.management.electronic.exceptions.BadRequestException;
import product.management.electronic.repository.UserRepository;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.impl.AuthServiceImpl;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    public void testLogin_Success() {
        AuthenticationDto authenticationDto = new AuthenticationDto("username", "password123","a@gmail.com");
        User mockUser = new User();
        mockUser.setUsername("username");
        mockUser.setPassword("$2a$10$hashedPassword123");
        mockUser.setActive(true);
        mockUser.setId(UUID.fromString("0634ef97-12bb-45d6-9903-7d498f3e4226"));
        mockUser.setEmail("user@example.com");
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword123")).thenReturn(true);
        when(jwtTokenService.createToken("username")).thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI");
        when(jwtTokenService.createRefreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI"))
                .thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0Nzg1MjA1fQ.OE6TuHwnPiI_cPv9qdfgQr2oVF217MqwdMXzAKvphm4");
        LoginDto loginDto = authService.login(authenticationDto).getData();
        assertNotNull(loginDto);
        assertEquals("username", loginDto.getUsername());
        assertEquals("user@example.com", loginDto.getEmail());
        assertEquals("jwtToken123", loginDto.getAccessToken());
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0Nzc3NzA0fQ.-pG9ImWieZwzdXEbMryD1IY2-2lxzpUuRMiK6ZUVG9o", loginDto.getRefreshToken());
        verify(userRepository).save(mockUser);
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0Nzc3NzA0fQ.-pG9ImWieZwzdXEbMryD1IY2-2lxzpUuRMiK6ZUVG9o", mockUser.getRefreshToken());
    }


    @Test
    public void testLogin_UserNotFound() {
        AuthenticationDto authenticationDto = new AuthenticationDto("nonExistentUser", "password123","a@gmail.com");
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(java.util.Optional.empty());
        assertThrows(BadRequestException.class, () -> authService.login(authenticationDto));
    }

    @Test
    public void testLogin_AccountNotActivated() {
        AuthenticationDto authenticationDto = new AuthenticationDto("username", "password123","a@gmail.com");
        User mockUser = new User();
        mockUser.setUsername("username");
        mockUser.setPassword("$2a$10$hashedPassword123");
        mockUser.setActive(false);
        when(userRepository.findByUsername("username")).thenReturn(java.util.Optional.of(mockUser));
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(authenticationDto));
        assertEquals("Account not activated. Please check your email to activate!", exception.getMessage());
    }

    @Test
    public void testLogin_PasswordMismatch() {
        AuthenticationDto authenticationDto = new AuthenticationDto("username", "wrongPassword","a@gmail.com");
        User mockUser = new User();
        mockUser.setUsername("username");
        mockUser.setPassword("$2a$10$hashedPassword123");
        mockUser.setActive(true);
        when(userRepository.findByUsername("username")).thenReturn(java.util.Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword123")).thenReturn(false);
        assertThrows(BadRequestException.class, () -> authService.login(authenticationDto));
    }

    @Test
    public void testLogout_Success() {
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI";
        User mockUser = new User();
        mockUser.setUsername("username");
        mockUser.setRefreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0Nzg1MjA1fQ.OE6TuHwnPiI_cPv9qdfgQr2oVF217MqwdMXzAKvphm4");
        when(jwtTokenService.verifyExpiration(authHeader)).thenReturn(true);
        when(userService.getUserByRefreshToken(authHeader)).thenReturn(mockUser);
        authService.logout(authHeader);
        assertNull(mockUser.getRefreshToken());
        verify(userService).save(mockUser);
    }

    @Test
    public void testLogout_TokenExpired() {
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI";
        when(jwtTokenService.verifyExpiration(authHeader)).thenReturn(false);
        authService.logout(authHeader);
        verify(userService, never()).getUserByRefreshToken(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    public void testLogout_UserNotFound() {
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI";
        when(jwtTokenService.verifyExpiration(authHeader)).thenReturn(true);
        when(userService.getUserByRefreshToken(authHeader)).thenReturn(null);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.logout(authHeader));
        assertEquals(TOKEN_INVALID, exception.getMessage());
    }

    @Test
    public void testLogin_SuccessWithAllFields() {
        AuthenticationDto authenticationDto = new AuthenticationDto("testuser", "password123","a@gmail.com");
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");
        mockUser.setEmail("string@gmail.com");
        mockUser.setPassword("$2a$10$hashedPassword123");
        mockUser.setActive(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword123")).thenReturn(true);
        when(jwtTokenService.createToken("testuser")).thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI");
        when(jwtTokenService.createRefreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI"))
                .thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0Nzg1MjA1fQ.OE6TuHwnPiI_cPv9qdfgQr2oVF217MqwdMXzAKvphm4");
        ApiResponse<LoginDto> response = authService.login(authenticationDto);
        LoginDto loginDto = response.getData();
        assertNotNull(loginDto);
        assertEquals(userId, loginDto.getId());
        assertEquals("testuser", loginDto.getUsername());
        assertEquals("string@gmail.com", loginDto.getEmail());
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI", loginDto.getAccessToken());
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0Nzg1MjA1fQ.OE6TuHwnPiI_cPv9qdfgQr2oVF217MqwdMXzAKvphm4", loginDto.getRefreshToken());
        verify(userRepository).save(mockUser);
    }
}