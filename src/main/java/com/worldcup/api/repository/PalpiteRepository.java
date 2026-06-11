package com.worldcup.api.repository;

import com.worldcup.api.entity.Palpite;
import com.worldcup.api.entity.Partida;
import com.worldcup.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PalpiteRepository extends JpaRepository<Palpite, UUID> {
    List<Palpite> findByUsuario(Usuario usuario);
    Optional<Palpite> findByUsuarioAndPartida(Usuario usuario, Partida partida);

    @Query("""
        SELECT u.id, u.nome,
               COALESCE(SUM(p.pontosGanhos), 0) as totalPalpites,
               COALESCE(pb.pontosBonus, 0) as pontosBonus
        FROM Usuario u
        LEFT JOIN Palpite p ON p.usuario = u
        LEFT JOIN PalpiteBonus pb ON pb.usuario = u
        WHERE u.ativo = true
        GROUP BY u.id, u.nome, pb.pontosBonus
        ORDER BY (COALESCE(SUM(p.pontosGanhos), 0) + COALESCE(pb.pontosBonus, 0)) DESC, u.nome ASC
    """)
    List<Object[]> findRanking();

    @Query("""
        SELECT COUNT(p) FROM Palpite p
        WHERE p.usuario = :usuario AND p.pontosGanhos = 10
    """)
    Long countAcertosExatos(Usuario usuario);
}
