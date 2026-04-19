package com.idve.backend.service;

import com.idve.backend.entity.User;
import com.idve.backend.exception.BadRequestException;
import com.idve.backend.repository.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentUploadService {

    private static final String UPLOADS_DIR = "uploads";

    private final UserRepository userRepository;

    public DocumentUploadService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User uploadForUser(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose a file to upload.");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("User not found"));

        Path uploadRoot = Paths.get(UPLOADS_DIR).toAbsolutePath().normalize();
        String extension = extractExtension(file.getOriginalFilename());
        String uniqueName = UUID.randomUUID() + extension;
        Path destination = uploadRoot.resolve(uniqueName).normalize();

        if (!destination.startsWith(uploadRoot)) {
            throw new BadRequestException("Invalid upload path");
        }

        try {
            Files.createDirectories(uploadRoot);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store uploaded file", ex);
        }

        String relativePath = UPLOADS_DIR + "/" + uniqueName;
        user.setDocumentPath(relativePath);
        user.setVerificationStatus("PENDING");
        return userRepository.save(user);
    }

    private String extractExtension(String originalFilename) {
        String cleanedName = Optional.ofNullable(originalFilename).orElse("");
        int dotIndex = cleanedName.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == cleanedName.length() - 1) {
            return "";
        }

        String extension = cleanedName.substring(dotIndex).replaceAll("[^A-Za-z0-9.]", "");
        if (extension.length() > 12) {
            return extension.substring(0, 12);
        }

        return extension.toLowerCase();
    }
}
