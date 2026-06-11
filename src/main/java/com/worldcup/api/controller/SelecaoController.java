package com.worldcup.api.controller;

import com.worldcup.api.entity.Selecao;
import com.worldcup.api.repository.SelecaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/selecoes")
@RequiredArgsConstructor
public class SelecaoController {

    private final SelecaoRepository selecaoRepository;

    @GetMapping
    public ResponseEntity<List<Selecao>> findAll() {
        return ResponseEntity.ok(selecaoRepository.findAll());
    }

    @GetMapping("/grupos")
    public ResponseEntity<Map<String, List<Selecao>>> porGrupo() {
        Map<String, List<Selecao>> grupos = selecaoRepository.findAll().stream()
                .collect(Collectors.groupingBy(Selecao::getGrupo));
        return ResponseEntity.ok(grupos);
    }
}
