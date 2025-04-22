package product.management.electronic.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import product.management.electronic.dto.Auth.AuthDto;
import product.management.electronic.dto.User.*;
import product.management.electronic.entities.User;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.UserService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/resend-activation")
    public ResponseEntity<ApiResponse> resendActivationToken(@RequestParam String email) throws MessagingException, IOException {
        userService.resendActivationToken(email);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Check your email"));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) throws MessagingException, IOException {
        userService.forgotPassword(email);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Check your email"));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/changePassword")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody ResetPasswordDto request) {
        AuthDto passwordDto = userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), passwordDto));
    }

    @Operation(summary = "Get all user")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), users));
    }

    @Operation(summary = "Get current user")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDto userDto = userService.findByUsername(username);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), userDto));
    }

    @Operation(summary = "Get user by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/getById/{userId}")
    public ResponseEntity<ApiResponse> getUser(@PathVariable("userId") UUID userId) {
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), userDto));
    }

    @PreAuthorize("(#userId == @userServiceImpl.getCurrentUserId()) or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/update/{userId}")
    public ResponseEntity<ApiResponse> updateUserById(@PathVariable("userId") String userId, @Valid @RequestBody UpdateUserDto request) {
        UserDto updatedUser = userService.updateUserById(userId, request);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), updatedUser));
    }
}