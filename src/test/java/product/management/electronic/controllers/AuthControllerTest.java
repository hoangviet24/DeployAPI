package product.management.electronic.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import product.management.electronic.dto.Auth.*;
import product.management.electronic.exceptions.UnauthorizedException;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.AuthService;
import product.management.electronic.services.JwtTokenService;
import product.management.electronic.services.UserService;
import java.io.IOException;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testLogin_Success() throws Exception {
        AuthenticationDto request = new AuthenticationDto("username", "password123","a@gmail.com");
        LoginDto loginDto = new LoginDto(
                UUID.fromString("123e4567-e89b-12d3-a456-556642440000"),
                "username",
                "user@example.com",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0Nzg1MjA1fQ.OE6TuHwnPiI_cPv9qdfgQr2oVF217MqwdMXzAKvphm4"
        );
        ApiResponse<LoginDto> response = new ApiResponse<>(200, "Success", loginDto);

        when(authService.login(any(AuthenticationDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0NzAyNDA1LCJqd3RJZCI6IjVlZTA1NmMwLTU3ZWItNGM3My1iY2I2LWYzMWJkMzYzYjFmNyJ9.d2gIlH_ZDTEAqUYRsMxpTcpA-4TH1bniMpXDPkDeaeI"))
                .andExpect(jsonPath("$.data.refreshToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInVzZXJOYW1lIjoic3RyaW5nIiwiZXhwIjoxNzQ0Nzg1MjA1fQ.OE6TuHwnPiI_cPv9qdfgQr2oVF217MqwdMXzAKvphm4"));
    }


    @Test
    public void testRegisterUser_Success() throws Exception, MessagingException, IOException {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("string@gmail.com");
        registerDto.setPassword("Password123");

        AuthDto authDto = new AuthDto();
        authDto.setUsername("newuser");
        authDto.setEmail("string@gmail.com");

        when(userService.registerUser(any(RegisterDto.class))).thenReturn(authDto);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("string@gmail.com"));

        verify(userService, times(1)).registerUser(any(RegisterDto.class));
    }

    @Test
    public void testActivateAccount_Success() throws Exception {
        String activationToken = "valid-activation-token";
        doNothing().when(userService).activateAccount(activationToken);

        mockMvc.perform(get("/api/v1/auth/activate")
                        .param("token", activationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Successfully"));

        verify(userService, times(1)).activateAccount(activationToken);
    }

    @Test
    public void testRefreshToken_Success() throws Exception {
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";

        when(jwtTokenService.createRefreshToken(refreshToken)).thenReturn(newAccessToken);

        mockMvc.perform(post("/api/v1/auth/refreshToken")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.token").value(newAccessToken));

        verify(jwtTokenService, times(1)).createRefreshToken(refreshToken);
    }

    @Test
    public void testRefreshToken_MissingHeader() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refreshToken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRefreshToken_InvalidHeader() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refreshToken")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogout_Success() throws Exception {
        String accessToken = "valid-access-token";

        doNothing().when(authService).logout(accessToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authService, times(1)).logout(accessToken);
    }

    @Test
    public void testLogout_MissingHeader() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogout_InvalidHeader() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogout_UnauthorizedException() throws Exception {
        String accessToken = "expired-token";

        doThrow(new UnauthorizedException("Token expired")).when(authService).logout(accessToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_IMPLEMENTED.value()))
                .andExpect(jsonPath("$.message").value("NOT_IMPLEMENTED"));
    }
}