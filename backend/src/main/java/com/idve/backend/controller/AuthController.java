package com.idve.backend.controller;

import com.idve.backend.dto.AuthResponse;
import com.idve.backend.dto.LoginRequest;
import com.idve.backend.dto.MessageResponse;
import com.idve.backend.dto.RegisterRequest;
import com.idve.backend.dto.SendOtpRequest;
import com.idve.backend.dto.SendOtpResponse;
import com.idve.backend.dto.UserResponse;
import com.idve.backend.dto.VerifyOtpRequest;
import com.idve.backend.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String githubClientId;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<SendOtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String otp = authService.sendOtp(request);
        String message = otp == null
            ? "OTP sent successfully"
            : "OTP generated in dev fallback mode";
        return ResponseEntity.ok(new SendOtpResponse(message, otp));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtpAndRegister(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth-config")
    public ResponseEntity<Map<String, Boolean>> oauthConfigStatus() {
        boolean googleConfigured = isConfigured(googleClientId, "replace-with-google-client-id");
        boolean githubConfigured = isConfigured(githubClientId, "replace-with-github-client-id");
        return ResponseEntity.ok(Map.of(
            "google", googleConfigured,
            "github", githubConfigured
        ));
    }

    private boolean isConfigured(String value, String placeholder) {
        return value != null && !value.isBlank() && !placeholder.equals(value);
    }
}
