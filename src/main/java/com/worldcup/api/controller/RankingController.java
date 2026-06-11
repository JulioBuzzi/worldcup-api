package com.worldcup.api.controller;

import com.worldcup.api.entity.Usuario;
import com.worldcup.api.repository.PalpiteRepository;
import com.worldcup.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final PalpiteRepository palpiteRepository;
    private final UsuarioRepository usuarioRepository;

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

            // Pular admin do ranking
            if (usuarioOpt.isPresent() &&
                usuarioOpt.get().getRole().name().equals("ADMIN")) {
                continue;
            }

            long acertosExatos = usuarioOpt
                    .map(palpiteRepository::countAcertosExatos)
                    .orElse(0L);

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
}