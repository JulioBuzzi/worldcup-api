package com.worldcup.api.entity;

import com.worldcup.api.entity.enums.FasePartida;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "partida", schema = "worldcup")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "selecao_casa_id", nullable = false)
    private Selecao selecaoCasa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "selecao_visitante_id", nullable = false)
    private Selecao selecaoVisitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FasePartida fase;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    @Column(name = "gols_casa", nullable = false)
    private Short golsCasa = 0;

    @Column(name = "gols_visitante", nullable = false)
    private Short golsVisitante = 0;

    @Column(nullable = false)
    private Boolean encerrada = false;

    private Integer rodada;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;
}
