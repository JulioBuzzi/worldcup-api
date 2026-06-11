package com.worldcup.api.dto.request;

import com.worldcup.api.entity.enums.FasePartida;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PartidaRequest(
        @NotNull UUID selecaoCasaId,
        @NotNull UUID selecaoVisitanteId,
        @NotNull FasePartida fase,
        @NotNull OffsetDateTime dataHora,
        Integer rodada
) {}
