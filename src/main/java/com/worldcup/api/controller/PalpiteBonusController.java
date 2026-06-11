package com.worldcup.api.controller;

import com.worldcup.api.dto.request.PalpiteBonusRequest;
import com.worldcup.api.entity.PalpiteBonus;
import com.worldcup.api.entity.Usuario;
import com.worldcup.api.repository.PalpiteBonusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
public class PalpiteBonusController {

    private final PalpiteBonusRepository bonusRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> salvar(@RequestBody PalpiteBonusRequest req,
                                                       @AuthenticationPrincipal Usuario usuario) {
        PalpiteBonus bonus = bonusRepository.findByUsuario(usuario)
                .orElse(new PalpiteBonus());
        bonus.setUsuario(usuario);
        bonus.setCampeao(req.campeao());
        bonus.setNeymarGol(req.neymarGol());
        bonus.setArtilheiro(req.artilheiro());
        bonus.setBrasilFase(req.brasilFase());
        PalpiteBonus saved = bonusRepository.save(bonus);
        return ResponseEntity.ok(toMap(saved));
    }

    @GetMapping("/meu")
    public ResponseEntity<Map<String, Object>> meuBonus(@AuthenticationPrincipal Usuario usuario) {
        return bonusRepository.findByUsuario(usuario)
                .map(b -> ResponseEntity.ok(toMap(b)))
                .orElse(ResponseEntity.noContent().build());
    }

    private Map<String, Object> toMap(PalpiteBonus b) {
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
        return map;
    }
}