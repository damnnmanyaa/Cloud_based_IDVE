package com.idve.backend.controller;

import com.idve.backend.dto.UserResponse;
import com.idve.backend.entity.User;
import com.idve.backend.exception.BadRequestException;
import com.idve.backend.exception.UnauthorizedException;
import com.idve.backend.repository.UserRepository;
import com.idve.backend.service.DocumentUploadService;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final DocumentUploadService documentUploadService;

    public UserController(UserRepository userRepository,
                          DocumentUploadService documentUploadService) {
        this.userRepository = userRepository;
        this.documentUploadService = documentUploadService;
    }

    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new BadRequestException("User not found"));

        String role = (user.getRole() == null || user.getRole().isBlank()) ? "USER" : user.getRole();
        String verificationStatus = (user.getVerificationStatus() == null || user.getVerificationStatus().isBlank())
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

    @PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file,
                                                              Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException("Unauthorized user");
        }

        User user = documentUploadService.uploadForUser(principal.getName(), file);

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("message", "Document uploaded successfully.");
        payload.put("verificationStatus", user.getVerificationStatus());
        payload.put("documentPath", user.getDocumentPath());
        return ResponseEntity.ok(payload);
    }
}
