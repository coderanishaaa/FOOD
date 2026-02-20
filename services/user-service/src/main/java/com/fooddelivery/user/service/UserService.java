package com.fooddelivery.user.service;

import com.fooddelivery.user.dto.*;
import com.fooddelivery.user.entity.Role;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.exception.DuplicateResourceException;
import com.fooddelivery.user.exception.ResourceNotFoundException;
import com.fooddelivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Register a new user. Validates uniqueness of email/phone,
     * encodes password, and returns JWT token.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Check for duplicate phone
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already registered: " + request.getPhone());
        }

        // Build and save user entity
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .address(request.getAddress())
                .build();

        user = userRepository.save(user);
        log.info("User registered: {} with role {}", user.getEmail(), user.getRole());

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    /**
     * Authenticate user with email and password, return JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtService.generateToken(user);
        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    /**
     * Get user profile by ID.
     */
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    /**
     * Get all users (admin only).
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role (e.g., all delivery agents).
     */
    public List<UserDto> getUsersByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase())).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Manual mapper: User entity → UserDto.
     */
    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
