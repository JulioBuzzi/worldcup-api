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

import java.util.*;

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
    public ResponseEntity<Map<String, Object>> criarPartida(@Valid @RequestBody PartidaRequest req) {
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

        Partida saved = partidaRepository.save(partida);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "message", "Partida criada"));
    }

    @PutMapping("/partidas/{id}/placar")
    public ResponseEntity<Map<String, Object>> atualizarPlacar(@PathVariable UUID id,
                                                                @Valid @RequestBody PlacarRequest req) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));

        boolean encerrando = req.encerrada() && !partida.getEncerrada();

        partida.setGolsCasa(req.golsCasa().shortValue());
        partida.setGolsVisitante(req.golsVisitante().shortValue());
        partida.setEncerrada(req.encerrada());

        partidaRepository.save(partida);

        if (encerrando) {
            calcularPontos(partida);
        }

        return ResponseEntity.ok(Map.of("id", id, "message", "Placar atualizado"));
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
    public ResponseEntity<List<Map<String, Object>>> listarBonus() {
        try {
            List<PalpiteBonus> lista = bonusRepository.findAllWithUsuario();
            List<Map<String, Object>> result = lista.stream().map(b -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", b.getId());
                map.put("campeao", b.getCampeao());
                map.put("neymarGol", b.getNeymarGol());
                map.put("artilheiro", b.getArtilheiro());
                map.put("brasilFase", b.getBrasilFase());
                map.put("campeaoAcertou", b.getCampeaoAcertou());
                map.put("neymarGolAcertou", b.getNeymarGolAcertou());
                map.put("artilheiroAcertou", b.getArtilheiroAcertou());
                map.put("brasilFaseAcertou", b.getBrasilFaseAcertou());
                map.put("pontosBonus", b.getPontosBonus());

                if (b.getUsuario() != null) {
                    Map<String, Object> usuario = new LinkedHashMap<>();
                    usuario.put("id", b.getUsuario().getId());
                    usuario.put("nome", b.getUsuario().getNome());
                    usuario.put("email", b.getUsuario().getEmail());
                    map.put("usuario", usuario);
                }
                return map;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @PutMapping("/bonus/corrigir")
    public ResponseEntity<Map<String, Object>> corrigirBonus(@RequestBody CorrecaoBonusRequest req) {
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
        bonusRepository.save(bonus);

        return ResponseEntity.ok(Map.of("id", bonus.getId(), "pontosBonus", pts));
    }

    // ── USUÁRIOS ──────────────────────────────────────────
    @GetMapping("/usuarios")
    public ResponseEntity<List<Map<String, Object>>> listarUsuarios() {
        List<Map<String, Object>> result = usuarioRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("nome", u.getNome());
            map.put("email", u.getEmail());
            map.put("role", u.getRole());
            map.put("ativo", u.getAtivo());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }
}