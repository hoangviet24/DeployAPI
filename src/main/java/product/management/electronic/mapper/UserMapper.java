package product.management.electronic.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import product.management.electronic.dto.Auth.LoginDto;
import product.management.electronic.dto.Auth.RegisterDto;
import product.management.electronic.dto.User.UserDto;
import product.management.electronic.entities.Role;
import product.management.electronic.entities.User;
import product.management.electronic.enums.RoleType;
import product.management.electronic.exceptions.ResourceNotFoundException;
import product.management.electronic.repository.RoleRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserMapper(PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public List<UserDto> toListDto(List<User> users) {
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFullname(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().stream().map(role -> role.getName().name()).collect(Collectors.toSet()),
                user.isActive(),
                user.getCreateAt()
        );
    }
    public User toEntityGoogle(LoginDto request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found")));
        user.setRole(roles);
        return user;
    }
    public User toEntity(RegisterDto request) {
        User user = new User();
        user.setFullname(request.getFullname());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty() || request.getRoles().contains("string")) {
            roles.add(roleRepository.findByName(RoleType.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role not found")));
        } else {
            Set<String> validRoles = Arrays.stream(RoleType.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            for (String role : request.getRoles()) {
                if (!validRoles.contains(role.toUpperCase())) {
                    throw new ResourceNotFoundException("Role not found: " + role);
                }

                RoleType roleType = RoleType.valueOf(role.toUpperCase());
                Role foundRole = roleRepository.findByName(roleType)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role));
                roles.add(foundRole);
            }
        }
        user.setRole(roles);
        return user;
    }

    public UserDto toLoginDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFullname(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().stream().map(role -> role.getName().name()).collect(Collectors.toSet()),
                user.isActive(),
                user.getCreateAt()
        );
    }

}