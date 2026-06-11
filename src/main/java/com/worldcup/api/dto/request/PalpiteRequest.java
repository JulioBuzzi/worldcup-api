package com.worldcup.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PalpiteRequest(
        @NotNull UUID partidaId,
        @NotNull @Min(0) Integer golsCasa,
        @NotNull @Min(0) Integer golsVisitante
) {}
