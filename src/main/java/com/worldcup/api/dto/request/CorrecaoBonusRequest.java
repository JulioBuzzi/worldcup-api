package com.worldcup.api.dto.request;

import java.util.UUID;

public record CorrecaoBonusRequest(
        UUID usuarioId,
        Boolean campeaoAcertou,
        Boolean neymarGolAcertou,
        Boolean artilheiroAcertou,
        Boolean brasilFaseAcertou
) {}
