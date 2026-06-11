package com.worldcup.api.controller;

import com.worldcup.api.entity.Partida;
import com.worldcup.api.entity.enums.FasePartida;
import com.worldcup.api.repository.PartidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/partidas")
@RequiredArgsConstructor
public class PartidaController {

    private final PartidaRepository partidaRepository;

    @GetMapping
    public ResponseEntity<List<Partida>> findAll() {
        return ResponseEntity.ok(partidaRepository.findAllByOrderByDataHoraAsc());
    }

    @GetMapping("/fase/{fase}")
    public ResponseEntity<List<Partida>> porFase(@PathVariable FasePartida fase) {
        return ResponseEntity.ok(partidaRepository.findByFaseOrderByDataHoraAsc(fase));
    }

    @GetMapping("/abertas")
    public ResponseEntity<List<Partida>> abertas() {
        return ResponseEntity.ok(
                partidaRepository.findByEncerradaFalseAndDataHoraAfterOrderByDataHoraAsc(
                        OffsetDateTime.now().minusHours(1)));
    }
}
