package com.finshield.service;

import com.finshield.dto.CreateUserRequest;
import com.finshield.dto.CreateUserResponse;
import com.finshield.entity.User;
import com.finshield.exception.NotFoundException;
import com.finshield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public CreateUserResponse create(CreateUserRequest req) {
        userRepository.findByEmail(req.email()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists: " + req.email());
        });

        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .country(req.country().toUpperCase())
                .createdAt(OffsetDateTime.now())
                .build();

        User saved = userRepository.save(user);

        return new CreateUserResponse(saved.getId(), saved.getFullName(), saved.getEmail(), saved.getCountry());
    }

    public User getOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}