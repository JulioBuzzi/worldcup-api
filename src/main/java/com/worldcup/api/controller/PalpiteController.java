package com.worldcup.api.controller;

import com.worldcup.api.dto.request.PalpiteRequest;
import com.worldcup.api.entity.Palpite;
import com.worldcup.api.entity.Partida;
import com.worldcup.api.entity.Usuario;
import com.worldcup.api.repository.PalpiteRepository;
import com.worldcup.api.repository.PartidaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/palpites")
@RequiredArgsConstructor
public class PalpiteController {

    private final PalpiteRepository palpiteRepository;
    private final PartidaRepository partidaRepository;

    @PostMapping
    public ResponseEntity<Palpite> salvar(@Valid @RequestBody PalpiteRequest req,
                                           @AuthenticationPrincipal Usuario usuario) {
        Partida partida = partidaRepository.findById(req.partidaId())
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));

        if (partida.getEncerrada()) {
            throw new IllegalArgumentException("Partida já encerrada");
        }

        Palpite palpite = palpiteRepository.findByUsuarioAndPartida(usuario, partida)
                .orElse(new Palpite());
        palpite.setUsuario(usuario);
        palpite.setPartida(partida);
        palpite.setGolsCasa(req.golsCasa().shortValue());
        palpite.setGolsVisitante(req.golsVisitante().shortValue());

        return ResponseEntity.ok(palpiteRepository.save(palpite));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<Palpite>> meusPalpites(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(palpiteRepository.findByUsuario(usuario));
    }
}
