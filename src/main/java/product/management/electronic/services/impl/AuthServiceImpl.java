package product.management.electronic.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import product.management.electronic.dto.Auth.AuthenticationDto;

import product.management.electronic.dto.Auth.LoginDto;
import product.management.electronic.entities.User;
import product.management.electronic.exceptions.BadRequestException;
import product.management.electronic.exceptions.ForbiddenException;
import product.management.electronic.repository.UserRepository;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.AuthService;
import product.management.electronic.services.JwtTokenService;
import product.management.electronic.services.UserService;

import static product.management.electronic.constants.MessageConstant.RESOURCE_NOT_FOUND;
import static product.management.electronic.constants.MessageConstant.TOKEN_INVALID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public ApiResponse<LoginDto> login(AuthenticationDto authenticationDto) {
        User user = userRepository.findByUsername(authenticationDto.getUsername())
                .orElseThrow(() -> new BadRequestException(RESOURCE_NOT_FOUND));
        if (!user.isActive()) {
            throw new ForbiddenException("Account not activated. Please check your email to activate!");
        }
        if (!passwordEncoder.matches(authenticationDto.getPassword(), user.getPassword())) {
            throw new BadRequestException(RESOURCE_NOT_FOUND);
        }
        String jwtToken = jwtTokenService.createToken(user.getUsername());
        String refreshToken = jwtTokenService.createRefreshToken(jwtToken);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        LoginDto loginDto = new LoginDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                jwtToken,
                refreshToken
        );
        return new ApiResponse<>(200, "Login successful!", loginDto);
    }

    public void logout(String authorizationHeader) {
        if (jwtTokenService.verifyExpiration(authorizationHeader)) {
            User user = userService.getUserByRefreshToken(authorizationHeader);
            if (user == null) {
                throw new BadRequestException(TOKEN_INVALID);
            }
            user.setRefreshToken(null);
            userService.save(user);
        }
    }

}
