package com.worldcup.api.controller;

import com.worldcup.api.dto.request.PalpiteBonusRequest;
import com.worldcup.api.entity.PalpiteBonus;
import com.worldcup.api.entity.Usuario;
import com.worldcup.api.repository.PalpiteBonusRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
public class PalpiteBonusController {

    private final PalpiteBonusRepository bonusRepository;

    @PostMapping
    public ResponseEntity<PalpiteBonus> salvar(@RequestBody PalpiteBonusRequest req,
                                                @AuthenticationPrincipal Usuario usuario) {
        PalpiteBonus bonus = bonusRepository.findByUsuario(usuario)
                .orElse(new PalpiteBonus());
        bonus.setUsuario(usuario);
        bonus.setCampeao(req.campeao());
        bonus.setNeymarGol(req.neymarGol());
        bonus.setArtilheiro(req.artilheiro());
        bonus.setBrasilFase(req.brasilFase());
        return ResponseEntity.ok(bonusRepository.save(bonus));
    }

    @GetMapping("/meu")
    public ResponseEntity<PalpiteBonus> meuBonus(@AuthenticationPrincipal Usuario usuario) {
        return bonusRepository.findByUsuario(usuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
