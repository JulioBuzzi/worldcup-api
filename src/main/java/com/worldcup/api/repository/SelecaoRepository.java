package com.worldcup.api.repository;

import com.worldcup.api.entity.Selecao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SelecaoRepository extends JpaRepository<Selecao, UUID> {}
