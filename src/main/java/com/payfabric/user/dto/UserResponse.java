package com.payfabric.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String status
) {}