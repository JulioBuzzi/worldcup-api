package com.worldcup.api.dto.response;

public record AuthResponse(
        String token,
        String nome,
        String email,
        String role
) {}
