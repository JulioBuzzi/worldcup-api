package com.worldcup.api.controller;

import com.worldcup.api.entity.Palpite;
import com.worldcup.api.entity.Partida;
import com.worldcup.api.entity.Usuario;
import com.worldcup.api.repository.PalpiteRepository;
import com.worldcup.api.repository.PartidaRepository;
import com.worldcup.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final PalpiteRepository palpiteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PartidaRepository partidaRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> ranking() {
        List<Object[]> raw = palpiteRepository.findRanking();
        List<Map<String, Object>> result = new ArrayList<>();
        int pos = 1;

        for (Object[] row : raw) {
            UUID userId = (UUID) row[0];
            String nome = (String) row[1];
            long pontosPartidas = ((Number) row[2]).longValue();
            long pontosBonus = ((Number) row[3]).longValue();
            long total = pontosPartidas + pontosBonus;

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);
            if (usuarioOpt.isPresent() && usuarioOpt.get().getRole().name().equals("ADMIN")) continue;

            long acertosExatos = usuarioOpt.map(palpiteRepository::countAcertosExatos).orElse(0L);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("posicao", pos++);
            entry.put("usuarioId", userId);
            entry.put("nome", nome);
            entry.put("role", usuarioOpt.map(u -> u.getRole().name()).orElse("USER"));
            entry.put("pontosPartidas", pontosPartidas);
            entry.put("pontosBonus", pontosBonus);
            entry.put("totalPontos", total);
            entry.put("acertosExatos", acertosExatos);
            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }

    // Retorna palpites de um usuário — só de partidas que já fecharam para palpite
    @GetMapping("/palpites/{usuarioId}")
    public ResponseEntity<List<Map<String, Object>>> palpitesUsuario(@PathVariable UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Deadline = 1h antes do jogo
        OffsetDateTime agora = OffsetDateTime.now();
        List<Partida> partidasFechadas = partidaRepository.findAllByOrderByDataHoraAsc().stream()
                .filter(p -> p.getEncerrada() || p.getDataHora().minusHours(1).isBefore(agora))
                .toList();

        List<Palpite> palpites = palpiteRepository.findByUsuario(usuario);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Palpite p : palpites) {
            // Só inclui se a partida já fechou
            boolean fechada = partidasFechadas.stream()
                    .anyMatch(pf -> pf.getId().equals(p.getPartida().getId()));
            if (!fechada) continue;

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("golsCasa", p.getGolsCasa());
            map.put("golsVisitante", p.getGolsVisitante());
            map.put("pontosGanhos", p.getPontosGanhos());

            if (p.getPartida() != null) {
                Map<String, Object> partida = new LinkedHashMap<>();
                partida.put("id", p.getPartida().getId());
                partida.put("fase", p.getPartida().getFase());
                partida.put("dataHora", p.getPartida().getDataHora());
                partida.put("encerrada", p.getPartida().getEncerrada());
                if (p.getPartida().getSelecaoCasa() != null) {
                    partida.put("selecaoCasa", Map.of(
                        "id", p.getPartida().getSelecaoCasa().getId(),
                        "nome", p.getPartida().getSelecaoCasa().getNome(),
                        "codigoFifa", p.getPartida().getSelecaoCasa().getCodigoFifa()
                    ));
                }
                if (p.getPartida().getSelecaoVisitante() != null) {
                    partida.put("selecaoVisitante", Map.of(
                        "id", p.getPartida().getSelecaoVisitante().getId(),
                        "nome", p.getPartida().getSelecaoVisitante().getNome(),
                        "codigoFifa", p.getPartida().getSelecaoVisitante().getCodigoFifa()
                    ));
                }
                map.put("partida", partida);
            }
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }
}