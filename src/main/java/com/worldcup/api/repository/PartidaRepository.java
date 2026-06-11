package com.worldcup.api.repository;

import com.worldcup.api.entity.Partida;
import com.worldcup.api.entity.enums.FasePartida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PartidaRepository extends JpaRepository<Partida, UUID> {
    List<Partida> findByFaseOrderByDataHoraAsc(FasePartida fase);
    List<Partida> findByEncerradaFalseAndDataHoraAfterOrderByDataHoraAsc(OffsetDateTime now);
    List<Partida> findAllByOrderByDataHoraAsc();
}
