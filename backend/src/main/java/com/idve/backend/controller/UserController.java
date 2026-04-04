package com.idve.backend.controller;

import com.idve.backend.dto.UserResponse;
import com.idve.backend.entity.User;
import com.idve.backend.exception.BadRequestException;
import com.idve.backend.repository.UserRepository;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new BadRequestException("User not found"));

        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
