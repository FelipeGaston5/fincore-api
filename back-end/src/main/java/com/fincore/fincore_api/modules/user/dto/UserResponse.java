package com.fincore.fincore_api.modules.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    boolean enabled,
    Instant createdAt
) {}