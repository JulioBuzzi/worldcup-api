package com.worldcup.api.repository;

import com.worldcup.api.entity.PalpiteBonus;
import com.worldcup.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PalpiteBonusRepository extends JpaRepository<PalpiteBonus, UUID> {
    Optional<PalpiteBonus> findByUsuario(Usuario usuario);
    List<PalpiteBonus> findAll();
}
