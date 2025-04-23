package product.management.electronic.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import product.management.electronic.dto.Auth.*;
import product.management.electronic.exceptions.UnauthorizedException;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.AuthService;
import product.management.electronic.services.JwtTokenService;
import product.management.electronic.services.UserService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    public AuthController(AuthService authService, JwtTokenService jwtTokenService, UserService userService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDto> registerUser(@RequestBody RegisterDto signupRequest) throws MessagingException, IOException {
        AuthDto response = userService.registerUser(signupRequest);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginDto>> authGoogle(@RequestBody AuthenticationDto authenticationDto) throws Exception {
        ApiResponse<LoginDto> response = authService.authGoogle(authenticationDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse> activateAccount(@RequestParam String token) {
        userService.activateAccount(token);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Active Successfully"));
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginDto>> login(@RequestBody AuthenticationDto authenticationDto) {
        ApiResponse<LoginDto> loginResponse = authService.login(authenticationDto);
        return ResponseEntity.ok(loginResponse);
    }
    @Operation(summary = "Refresh token")
    @PostMapping("/refreshToken")
    public ResponseEntity<ApiResponse> refreshToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header missing or invalid");
        }
        try {
            String refreshToken = authorizationHeader.replace("Bearer ", "");
            String token = jwtTokenService.createRefreshToken(refreshToken);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), new TokenDto(token)));
        } catch (UnauthorizedException e) {
            throw e;
        }
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Log out")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header missing or invalid");
        }
        try {
            authService.logout(authorizationHeader.replace("Bearer ", ""));
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Logout successful", null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new ApiResponse(HttpStatus.NOT_IMPLEMENTED.value(), "NOT_IMPLEMENTED", null));
        }
    }
}
