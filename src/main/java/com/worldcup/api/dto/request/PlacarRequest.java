package com.worldcup.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PlacarRequest(
        @NotNull @Min(0) Integer golsCasa,
        @NotNull @Min(0) Integer golsVisitante,
        @NotNull Boolean encerrada
) {}
