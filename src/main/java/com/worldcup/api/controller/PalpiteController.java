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
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/palpites")
@RequiredArgsConstructor
public class PalpiteController {

    private final PalpiteRepository palpiteRepository;
    private final PartidaRepository partidaRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> salvar(@Valid @RequestBody PalpiteRequest req,
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

        Palpite saved = palpiteRepository.save(palpite);
        return ResponseEntity.ok(toMap(saved));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<Map<String, Object>>> meusPalpites(@AuthenticationPrincipal Usuario usuario) {
        try {
            List<Palpite> palpites = palpiteRepository.findByUsuario(usuario);
            List<Map<String, Object>> result = palpites.stream()
                    .map(this::toMap)
                    .toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    private Map<String, Object> toMap(Palpite p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("golsCasa", p.getGolsCasa());
        map.put("golsVisitante", p.getGolsVisitante());
        map.put("pontosGanhos", p.getPontosGanhos());
        map.put("criadoEm", p.getCriadoEm());

        if (p.getPartida() != null) {
            Map<String, Object> partida = new LinkedHashMap<>();
            partida.put("id", p.getPartida().getId());
            partida.put("fase", p.getPartida().getFase());
            partida.put("dataHora", p.getPartida().getDataHora());
            partida.put("golsCasa", p.getPartida().getGolsCasa());
            partida.put("golsVisitante", p.getPartida().getGolsVisitante());
            partida.put("encerrada", p.getPartida().getEncerrada());

            if (p.getPartida().getSelecaoCasa() != null) {
                Map<String, Object> casa = new LinkedHashMap<>();
                casa.put("id", p.getPartida().getSelecaoCasa().getId());
                casa.put("nome", p.getPartida().getSelecaoCasa().getNome());
                casa.put("codigoFifa", p.getPartida().getSelecaoCasa().getCodigoFifa());
                partida.put("selecaoCasa", casa);
            }
            if (p.getPartida().getSelecaoVisitante() != null) {
                Map<String, Object> vis = new LinkedHashMap<>();
                vis.put("id", p.getPartida().getSelecaoVisitante().getId());
                vis.put("nome", p.getPartida().getSelecaoVisitante().getNome());
                vis.put("codigoFifa", p.getPartida().getSelecaoVisitante().getCodigoFifa());
                partida.put("selecaoVisitante", vis);
            }
            map.put("partida", partida);
        }
        return map;
    }
}