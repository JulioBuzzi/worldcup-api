package com.worldcup.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "palpite",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "partida_id"}))
public class Palpite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @Column(name = "gols_casa", nullable = false)
    private Short golsCasa;

    @Column(name = "gols_visitante", nullable = false)
    private Short golsVisitante;

    @Column(name = "pontos_ganhos")
    private Short pontosGanhos;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;
}
