package com.idve.backend.service;

import com.idve.backend.dto.AuthResponse;
import com.idve.backend.dto.LoginRequest;
import com.idve.backend.dto.RegisterRequest;
import com.idve.backend.dto.SendOtpRequest;
import com.idve.backend.dto.UserResponse;
import com.idve.backend.dto.VerifyOtpRequest;
import com.idve.backend.entity.User;
import com.idve.backend.exception.BadRequestException;
import com.idve.backend.exception.UnauthorizedException;
import com.idve.backend.repository.UserRepository;
import com.idve.backend.security.JwtUtil;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
    }

    public String sendOtp(SendOtpRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new BadRequestException("Email already exists");
        });
        return otpService.sendOtp(request.getEmail());
    }

    public AuthResponse verifyOtpAndRegister(VerifyOtpRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new BadRequestException("Email already exists");
        });

        otpService.verifyOtp(request.getEmail(), request.getOtp());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(normalizeRole(request.getRole()));
        user.setVerificationStatus("PENDING");
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole());
        return new AuthResponse(token);
    }

    public UserResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new BadRequestException("Email already exists");
        });

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setVerificationStatus("PENDING");

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole(), saved.getVerificationStatus());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("User not registered. Please sign up."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String role = normalizeRole(user.getRole());
        if (request.getRole() != null && !request.getRole().isBlank()) {
            String selectedRole = normalizeRole(request.getRole());
            if (!selectedRole.equals(role)) {
                throw new UnauthorizedException("Selected login type does not match your account role");
            }
        }

        String token = jwtUtil.generateToken(user.getEmail(), role);
        return new AuthResponse(token);
    }

    public String loginWithOAuth(String email, String displayName) {
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createOAuthUser(email, displayName));

        String role = (user.getRole() == null || user.getRole().isBlank()) ? "USER" : user.getRole();
        return jwtUtil.generateToken(user.getEmail(), role);
    }

    private User createOAuthUser(String email, String displayName) {
        User user = new User();
        user.setEmail(email);
        user.setName((displayName == null || displayName.isBlank()) ? email : displayName);
        // Random encoded password keeps schema compatibility for OAuth-only users.
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole("USER");
        user.setVerificationStatus("PENDING");
        return userRepository.save(user);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalized = role.toUpperCase().replace("ROLE_", "");
        return "ADMIN".equals(normalized) ? "ADMIN" : "USER";
    }
}
