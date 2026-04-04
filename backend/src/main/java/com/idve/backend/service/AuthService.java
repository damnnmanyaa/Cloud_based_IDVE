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
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getEmail());
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

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("User not registered. Please sign up."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    public String loginWithOAuth(String email, String displayName) {
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createOAuthUser(email, displayName));

        return jwtUtil.generateToken(user.getEmail());
    }

    private User createOAuthUser(String email, String displayName) {
        User user = new User();
        user.setEmail(email);
        user.setName((displayName == null || displayName.isBlank()) ? email : displayName);
        // Random encoded password keeps schema compatibility for OAuth-only users.
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        return userRepository.save(user);
    }
}
