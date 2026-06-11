package com.worldcup.api.controller;

import com.worldcup.api.dto.request.CorrecaoBonusRequest;
import com.worldcup.api.dto.request.PartidaRequest;
import com.worldcup.api.dto.request.PlacarRequest;
import com.worldcup.api.entity.*;
import com.worldcup.api.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final PartidaRepository partidaRepository;
    private final SelecaoRepository selecaoRepository;
    private final PalpiteRepository palpiteRepository;
    private final PalpiteBonusRepository bonusRepository;
    private final UsuarioRepository usuarioRepository;

    // ── PARTIDAS ──────────────────────────────────────────
    @PostMapping("/partidas")
    public ResponseEntity<Partida> criarPartida(@Valid @RequestBody PartidaRequest req) {
        Selecao casa = selecaoRepository.findById(req.selecaoCasaId())
                .orElseThrow(() -> new IllegalArgumentException("Seleção casa não encontrada"));
        Selecao visitante = selecaoRepository.findById(req.selecaoVisitanteId())
                .orElseThrow(() -> new IllegalArgumentException("Seleção visitante não encontrada"));

        Partida partida = new Partida();
        partida.setSelecaoCasa(casa);
        partida.setSelecaoVisitante(visitante);
        partida.setFase(req.fase());
        partida.setDataHora(req.dataHora());
        partida.setRodada(req.rodada());

        return ResponseEntity.ok(partidaRepository.save(partida));
    }

    @PutMapping("/partidas/{id}/placar")
    public ResponseEntity<Partida> atualizarPlacar(@PathVariable UUID id,
                                                    @Valid @RequestBody PlacarRequest req) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));

        partida.setGolsCasa(req.golsCasa().shortValue());
        partida.setGolsVisitante(req.golsVisitante().shortValue());

        boolean encerrando = req.encerrada() && !partida.getEncerrada();
        partida.setEncerrada(req.encerrada());

        Partida saved = partidaRepository.save(partida);

        // Calcular pontos se encerrada agora
        if (encerrando) {
            calcularPontos(saved);
        }

        return ResponseEntity.ok(saved);
    }

    private void calcularPontos(Partida partida) {
        List<Palpite> palpites = palpiteRepository.findAll().stream()
                .filter(p -> p.getPartida().getId().equals(partida.getId()))
                .toList();

        for (Palpite p : palpites) {
            short pontos;
            if (p.getGolsCasa().equals(partida.getGolsCasa()) &&
                p.getGolsVisitante().equals(partida.getGolsVisitante())) {
                pontos = 10;
            } else {
                int sinalPalpite = Integer.signum(p.getGolsCasa() - p.getGolsVisitante());
                int sinalReal = Integer.signum(partida.getGolsCasa() - partida.getGolsVisitante());
                pontos = (sinalPalpite == sinalReal) ? (short) 5 : (short) 0;
            }
            p.setPontosGanhos(pontos);
            palpiteRepository.save(p);
        }
    }

    // ── BÔNUS ─────────────────────────────────────────────
    @GetMapping("/bonus")
    public ResponseEntity<List<PalpiteBonus>> listarBonus() {
        return ResponseEntity.ok(bonusRepository.findAll());
    }

    @PutMapping("/bonus/corrigir")
    public ResponseEntity<PalpiteBonus> corrigirBonus(@RequestBody CorrecaoBonusRequest req) {
        Usuario usuario = usuarioRepository.findById(req.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        PalpiteBonus bonus = bonusRepository.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalArgumentException("Palpite bônus não encontrado"));

        bonus.setCampeaoAcertou(req.campeaoAcertou());
        bonus.setNeymarGolAcertou(req.neymarGolAcertou());
        bonus.setArtilheiroAcertou(req.artilheiroAcertou());
        bonus.setBrasilFaseAcertou(req.brasilFaseAcertou());

        int pts = 0;
        if (Boolean.TRUE.equals(req.campeaoAcertou()))     pts += 25;
        if (Boolean.TRUE.equals(req.neymarGolAcertou()))   pts += 10;
        if (Boolean.TRUE.equals(req.artilheiroAcertou()))  pts += 25;
        if (Boolean.TRUE.equals(req.brasilFaseAcertou()))  pts += 25;

        bonus.setPontosBonus(pts);
        return ResponseEntity.ok(bonusRepository.save(bonus));
    }

    // ── USUÁRIOS ──────────────────────────────────────────
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }
}
