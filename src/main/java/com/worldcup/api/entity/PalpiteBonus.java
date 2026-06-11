package com.worldcup.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "palpite_bonus", schema = "worldcup")
public class PalpiteBonus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    private String campeao;

    @Column(name = "neymar_gol")
    private Boolean neymarGol;

    private String artilheiro;

    @Column(name = "brasil_fase", length = 20)
    private String brasilFase;

    @Column(name = "campeao_acertou")
    private Boolean campeaoAcertou;

    @Column(name = "neymar_gol_acertou")
    private Boolean neymarGolAcertou;

    @Column(name = "artilheiro_acertou")
    private Boolean artilheiroAcertou;

    @Column(name = "brasil_fase_acertou")
    private Boolean brasilFaseAcertou;

    @Column(name = "pontos_bonus", nullable = false)
    private Integer pontosBonus = 0;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;
}
