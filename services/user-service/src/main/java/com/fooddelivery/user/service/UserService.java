package com.fooddelivery.user.service;

import com.fooddelivery.user.dto.*;
import com.fooddelivery.user.entity.Role;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.exception.DuplicateResourceException;
import com.fooddelivery.user.exception.ResourceNotFoundException;
import com.fooddelivery.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already registered: " + request.getPhone());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setAddress(request.getAddress());

        user = userRepository.save(user);
        log.info("User registered: {} with role {}", user.getEmail(), user.getRole());

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtService.generateToken(user);
        log.info("User logged in: {}", user.getEmail());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getId());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase())).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getAddress(),
                user.getCreatedAt());
    }
}
