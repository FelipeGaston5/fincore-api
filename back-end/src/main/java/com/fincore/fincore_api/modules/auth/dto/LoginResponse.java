package com.fincore.fincore_api.modules.auth.dto;

public record LoginResponse(
    String tokenType,
    String accessToken,
    long expiresIn,
    String email
) {}
