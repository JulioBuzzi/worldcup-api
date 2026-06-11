package com.worldcup.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String nome,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String senha
) {}
