package com.finshield.dto;

import java.util.UUID;

public record CreateUserResponse(
        UUID id,
        String fullName,
        String email,
        String country
) {}