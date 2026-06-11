package com.worldcup.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "selecao")
public class Selecao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(name = "codigo_fifa", nullable = false, length = 3, unique = true)
    private String codigoFifa;

    @Column(nullable = false, length = 2)
    private String grupo;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;
}
