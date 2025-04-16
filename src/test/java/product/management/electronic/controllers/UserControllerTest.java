package product.management.electronic.controllers;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import product.management.electronic.dto.Auth.AuthDto;
import product.management.electronic.dto.User.ResetPasswordDto;
import product.management.electronic.dto.User.UpdateUserDto;
import product.management.electronic.dto.User.UserDto;
import product.management.electronic.exceptions.ConflictException;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static product.management.electronic.constants.MessageConstant.USER_NOTFOUND;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = Mockito.mock(UserService.class);
        userController = new UserController(userService);
    }

    // Forgot Password
    @Test
    public void testForgotPassword_Success() throws MessagingException, IOException {
        String email = "test@example.com";

        ResponseEntity<ApiResponse> response = userController.forgotPassword(email);

        verify(userService).forgotPassword(email);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Successfully", response.getBody().getMessage());
    }

    @Test
    public void testForgotPassword_ThrowsException() throws MessagingException, IOException {
        String email = "test@example.com";
        doThrow(new MessagingException("Lỗi gửi mail")).when(userService).forgotPassword(email);

        MessagingException exception = assertThrows(MessagingException.class, () -> {
            userController.forgotPassword(email);
        });

        assertEquals("Lỗi gửi mail", exception.getMessage());
        verify(userService).forgotPassword(email);
    }

    // Change password
    @Test
    public void testChangePassword_Success() {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        UUID id = UUID.randomUUID();

        resetPasswordDto.setUsername("username");
        resetPasswordDto.setOldPassword("oldPass123");
        resetPasswordDto.setNewPassword("newPass456");

        AuthDto mockAuthDto = new AuthDto(
                UUID.randomUUID(),
                "testuser",
                "test@example.com",
                LocalDateTime.now()
        );

        when(userService.changePassword(resetPasswordDto.getUsername(),
                resetPasswordDto.getOldPassword(),
                resetPasswordDto.getNewPassword()))
                .thenReturn(mockAuthDto);

        ResponseEntity<ApiResponse> response = userController.changePassword(resetPasswordDto);

        verify(userService).changePassword(resetPasswordDto.getUsername(),
                resetPasswordDto.getOldPassword(),
                resetPasswordDto.getNewPassword());

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(mockAuthDto, response.getBody().getData());
    }

    @Test
    public void testChangePassword_ThrowsException() {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setUsername("test@example.com");
        resetPasswordDto.setOldPassword("wrongOld");
        resetPasswordDto.setNewPassword("newPass456");

        when(userService.changePassword(resetPasswordDto.getUsername(),
                resetPasswordDto.getOldPassword(),
                resetPasswordDto.getNewPassword()))
                .thenThrow(new IllegalArgumentException("Old password is not correct!"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userController.changePassword(resetPasswordDto);
        });

        assertEquals("Old password is not correct!", exception.getMessage());
        verify(userService).changePassword(resetPasswordDto.getUsername(),
                resetPasswordDto.getOldPassword(),
                resetPasswordDto.getNewPassword());
    }
    //Get all user
    @Test
    public void testGetAllUsers_Success() {
        List<UserDto> mockUsers = List.of(
                new UserDto(UUID.randomUUID(), "user1", "fullname1", "user1@example.com", "phone", "address", Set.of("ROLE_USER"), true, LocalDateTime.now()),
                new UserDto(UUID.randomUUID(), "user2", "fullname2", "user1@example.com", "phone", "address", Set.of("ROLE_ADMIN"), true, LocalDateTime.now())
        );

        when(userService.getAllUsers()).thenReturn(mockUsers);

        ResponseEntity<ApiResponse> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
        assertEquals(mockUsers, response.getBody().getData());

        verify(userService).getAllUsers();
    }
    // Get user By Id
    @Test
    public void testGetUserById_Success() {
        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto(userId, "user1", "fullname1", "user1@example.com",
                "phone", "address", Set.of("ROLE_USER"), true, LocalDateTime.now());

        when(userService.getUserById(userId)).thenReturn(userDto);

        ResponseEntity<ApiResponse> response = userController.getUser(userId);

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
        assertEquals(userDto, response.getBody().getData());

        verify(userService).getUserById(userId);
    }

    @Test
    public void testGetUserById_NotFound() {
        UUID userId = UUID.randomUUID();

        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException(USER_NOTFOUND + userId));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userController.getUser(userId);
        });

        assertEquals(USER_NOTFOUND + userId, exception.getMessage());
        verify(userService).getUserById(userId);
    }
    //Update user
    @Test
    public void testUpdateUserById_Success() {
        String userId = UUID.randomUUID().toString();
        UpdateUserDto updateRequest = new UpdateUserDto();
        updateRequest.setEmail("newemail@example.com");

        UserDto updatedUser = new UserDto(
                UUID.fromString(userId),
                "newUsername",
                "New Fullname",
                "newemail@example.com",
                "0123456789",
                "New Address",
                Set.of("ROLE_USER"),
                true,
                LocalDateTime.now()
        );

        when(userService.updateUserById(eq(userId), any(UpdateUserDto.class))).thenReturn(updatedUser);

        ResponseEntity<ApiResponse> response = userController.updateUserById(userId, updateRequest);

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode());
        assertEquals(updatedUser, response.getBody().getData());

        verify(userService).updateUserById(userId, updateRequest);
    }

    @Test
    public void testUpdateUserById_EmailConflict() {
        String userId = UUID.randomUUID().toString();
        UpdateUserDto updateRequest = new UpdateUserDto();
        updateRequest.setEmail("existing@example.com");

        when(userService.updateUserById(eq(userId), any(UpdateUserDto.class)))
                .thenThrow(new ConflictException("Email already exists!"));

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userController.updateUserById(userId, updateRequest);
        });

        assertEquals("Email already exists!", exception.getMessage());
        verify(userService).updateUserById(userId, updateRequest);
    }

}
