package com.idve.backend.service;

import com.idve.backend.dto.AuthResponse;
import com.idve.backend.dto.LoginRequest;
import com.idve.backend.entity.User;
import com.idve.backend.exception.UnauthorizedException;
import com.idve.backend.repository.UserRepository;
import com.idve.backend.security.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OtpService otpService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil, otpService);
    }

    @Test
    void login_CorrectPassword_ReturnsAuthResponseWithToken() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setRole("USER");
        user.setVerificationStatus("VERIFIED");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com", "USER")).thenReturn("fake-jwt-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("correct-password");

        AuthResponse response = authService.login(request);

        assertEquals("fake-jwt-token", response.getToken());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("correct-password", "encoded-password");
        verify(jwtUtil).generateToken("test@example.com", "USER");
    }

    @Test
    void login_IncorrectPassword_ThrowsUnauthorized() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setRole("USER");
        user.setVerificationStatus("VERIFIED");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong-password");

        UnauthorizedException ex = assertThrows(
            UnauthorizedException.class,
            () -> authService.login(request)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_UserNotFound_ThrowsUnauthorized() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("anypassword");

        UnauthorizedException ex = assertThrows(
            UnauthorizedException.class,
            () -> authService.login(request)
        );

        assertEquals("User not registered. Please sign up.", ex.getMessage());
        verify(passwordEncoder, never()).matches(any(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_RoleMismatch_ThrowsUnauthorized() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setRole("USER");
        user.setVerificationStatus("VERIFIED");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("correct-password");
        request.setRole("ADMIN");

        UnauthorizedException ex = assertThrows(
            UnauthorizedException.class,
            () -> authService.login(request)
        );

        assertEquals("Selected login type does not match your account role", ex.getMessage());
    }
}
