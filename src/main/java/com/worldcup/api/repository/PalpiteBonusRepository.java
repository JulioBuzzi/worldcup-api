package com.worldcup.api.repository;

import com.worldcup.api.entity.PalpiteBonus;
import com.worldcup.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PalpiteBonusRepository extends JpaRepository<PalpiteBonus, UUID> {
    Optional<PalpiteBonus> findByUsuario(Usuario usuario);

    @Query("SELECT pb FROM PalpiteBonus pb JOIN FETCH pb.usuario ORDER BY pb.criadoEm ASC")
    List<PalpiteBonus> findAllWithUsuario();
}