package com.idve.backend.controller;

import com.idve.backend.dto.UserResponse;
import com.idve.backend.entity.User;
import com.idve.backend.exception.BadRequestException;
import com.idve.backend.repository.UserRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return userRepository.findAll().stream()
            .map(this::toUserResponse)
            .toList();
    }

    @PatchMapping("/users/{id}/approve")
    public UserResponse approve(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("User not found"));

        user.setVerificationStatus("VERIFIED");
        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @PatchMapping("/users/{id}/reject")
    public UserResponse reject(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("User not found"));

        user.setVerificationStatus("REJECTED");
        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    private UserResponse toUserResponse(User user) {
        String role = (user.getRole() == null || user.getRole().isBlank()) ? "USER" : user.getRole();
        String verificationStatus =
            (user.getVerificationStatus() == null || user.getVerificationStatus().isBlank())
                ? "PENDING"
                : user.getVerificationStatus();

        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            role,
            verificationStatus,
            user.getDocumentPath()
        );
    }
}
