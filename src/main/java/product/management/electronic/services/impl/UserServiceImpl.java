package product.management.electronic.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import product.management.electronic.dto.Auth.AuthDto;
import product.management.electronic.dto.Auth.AuthenticationDto;
import product.management.electronic.dto.Auth.LoginDto;
import product.management.electronic.dto.Auth.RegisterDto;
import product.management.electronic.dto.User.UpdateUserDto;
import product.management.electronic.dto.User.UserDto;
import product.management.electronic.entities.User;
import product.management.electronic.exceptions.BadRequestException;
import product.management.electronic.exceptions.ConflictException;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.mapper.UserMapper;
import product.management.electronic.repository.UserRepository;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static product.management.electronic.constants.MessageConstant.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    @Value("${base_url}")
    private String baseUrl;

    public UserDetails loadUserByUsername(String username) throws ResourceNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException(USER_NOTFOUND));
        List<GrantedAuthority> authorities = user.getRole().stream().map(role -> new SimpleGrantedAuthority(role.getName().name())).collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BadRequestException(VALUE_NO_EXIST));
        return userMapper.toLoginDto(user);
    }

    public User getUserByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new BadRequestException(TOKEN_INVALID));
    }

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public AuthDto registerUser(RegisterDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already registered! " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered! " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setActive(false);

        String token = UUID.randomUUID().toString();

        try {
            sendEmailActivation(user.getEmail(), user.getUsername(), token);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Send email failed: " + e.getMessage());
        }

        user.setActivationToken(token);
        user.setActivationTokenExpirationTime(LocalDateTime.now().plusMinutes(15));
        User savedUser = userRepository.save(user);

        return new AuthDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getCreateAt()
        );
    }
    @Override
    public void activateAccount(String token) {
        Optional<User> userOptional = userRepository.findByActivationToken(token);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("Token is invalid or expired!");
        }

        User user = userOptional.get();
        if (user.getActivationTokenExpirationTime() != null &&
                user.getActivationTokenExpirationTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token is invalid or expired!.");
        }
        user.setActive(true);
        user.setActivationToken(null);
        user.setActivationTokenExpirationTime(null);
        userRepository.save(user);
    }
    @Override
    public void resendActivationToken(String email) throws MessagingException, IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOTFOUND));

        if (user.isActive()) {
            throw new RuntimeException("The account has been previously activated.");
        }

        String newToken = UUID.randomUUID().toString();
        user.setActivationToken(newToken);
        user.setActivationTokenExpirationTime(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        sendEmailActivation(user.getEmail(), user.getUsername(), token);
    }
    public void saveToken(String email, String token) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActivationToken(token);
            userRepository.save(user);
        }
    }

    @Override
    public AuthDto changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword == null) {
            throw new BadRequestException("New password cannot be null!");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is not correct!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new AuthDto(user.getId(), user.getUsername(), user.getEmail(), user.getCreateAt());
    }

    @Override
    public void forgotPassword(String email) throws MessagingException, IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found!"));

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        sendEmailResetPassword(email, "Quên mật khẩu", "Mật khẩu mới của bạn là: " + newPassword, user.getUsername());
    }

    public String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            stringBuilder.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }

        return stringBuilder.toString();
    }

    @Override
    public void sendEmailActivation(String to, String username, String token) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Kích hoạt tài khoản của bạn");

        String activationLink = baseUrl + "/api/v1/auth/activate?token=" + token;
        saveToken(to, token);

        // ✅ Đọc file template từ JAR bằng InputStream
        ClassPathResource resource = new ClassPathResource("templates/email_active.html");
        String htmlContent;
        try (InputStream inputStream = resource.getInputStream()) {
            htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Thay thế biến trong template
        htmlContent = htmlContent.replace("{{USER_NAME}}", username)
                .replace("{{ACTIVATION_LINK}}", activationLink);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }




    public void sendEmailResetPassword(String to, String subject, String newPassword, String username) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);

        ClassPathResource resource = new ClassPathResource("templates/email_password.html");
        InputStream inputStream = resource.getInputStream();
        String htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        htmlContent = htmlContent.replace("{{NEW_PASSWORD}}", newPassword)
                .replace("{{USER_NAME}}", username);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }


    @Override
    public List<UserDto> getAllUsers() {
        return userMapper.toListDto(userRepository.findAll());
    }

    @Override
    public UserDto getUserById(UUID id) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new BadRequestException(VALUE_NO_EXIST));
        return userMapper.toDto(user);
    }

    @Override
    public User getUserId(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(USER_NOTFOUND));
    }

    @Override
    public UserDto updateUserById(String id, UpdateUserDto request) {
        UUID userId = UUID.fromString(id);
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new BadRequestException(VALUE_NO_EXIST));
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new ConflictException("Email already registered! " + request.getEmail());
        }
        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        userRepository.save(user);
        return userMapper.toDto(user);
    }

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException(USER_NOTFOUND));
        return user.getId().toString();
    }
}