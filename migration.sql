-- =============================================
-- BOLÃO COPA DO MUNDO 2026 — Migration SQL
-- Rodar no Supabase SQL Editor
-- =============================================

CREATE SCHEMA IF NOT EXISTS worldcup;

-- ── TABELAS ──────────────────────────────────

CREATE TABLE worldcup.selecao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(80) NOT NULL,
    codigo_fifa VARCHAR(3) NOT NULL,
    grupo VARCHAR(2) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_selecao_codigo UNIQUE (codigo_fifa)
);

CREATE TABLE worldcup.usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha_hash TEXT NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

CREATE TABLE worldcup.partida (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    selecao_casa_id UUID NOT NULL REFERENCES worldcup.selecao(id),
    selecao_visitante_id UUID NOT NULL REFERENCES worldcup.selecao(id),
    fase VARCHAR(20) NOT NULL,
    data_hora TIMESTAMPTZ NOT NULL,
    gols_casa SMALLINT NOT NULL DEFAULT 0,
    gols_visitante SMALLINT NOT NULL DEFAULT 0,
    encerrada BOOLEAN NOT NULL DEFAULT FALSE,
    rodada INT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE worldcup.palpite (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES worldcup.usuario(id) ON DELETE CASCADE,
    partida_id UUID NOT NULL REFERENCES worldcup.partida(id) ON DELETE CASCADE,
    gols_casa SMALLINT NOT NULL CHECK (gols_casa >= 0),
    gols_visitante SMALLINT NOT NULL CHECK (gols_visitante >= 0),
    pontos_ganhos SMALLINT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_palpite_usuario_partida UNIQUE (usuario_id, partida_id)
);

CREATE TABLE worldcup.palpite_bonus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES worldcup.usuario(id) ON DELETE CASCADE,
    campeao VARCHAR(80),
    neymar_gol BOOLEAN,
    artilheiro VARCHAR(100),
    brasil_fase VARCHAR(20),
    campeao_acertou BOOLEAN,
    neymar_gol_acertou BOOLEAN,
    artilheiro_acertou BOOLEAN,
    brasil_fase_acertou BOOLEAN,
    pontos_bonus INT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_bonus_usuario UNIQUE (usuario_id)
);

-- ── TRIGGER PONTUAÇÃO ─────────────────────────

CREATE OR REPLACE FUNCTION worldcup.fn_calcula_pontos()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.encerrada = TRUE AND OLD.encerrada = FALSE THEN
        UPDATE worldcup.palpite p
        SET pontos_ganhos = CASE
            WHEN p.gols_casa = NEW.gols_casa AND p.gols_visitante = NEW.gols_visitante THEN 10
            WHEN SIGN(p.gols_casa - p.gols_visitante) = SIGN(NEW.gols_casa - NEW.gols_visitante) THEN 5
            ELSE 0
        END
        WHERE p.partida_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_calcula_pontos
    AFTER UPDATE OF encerrada ON worldcup.partida
    FOR EACH ROW EXECUTE FUNCTION worldcup.fn_calcula_pontos();

-- ── SELEÇÕES (48 times, grupos A-L) ───────────

INSERT INTO worldcup.selecao (nome, codigo_fifa, grupo) VALUES
-- Grupo A
('México', 'MEX', 'A'),
('África do Sul', 'RSA', 'A'),
('Coreia do Sul', 'KOR', 'A'),
('República Tcheca', 'CZE', 'A'),
-- Grupo B
('Canadá', 'CAN', 'B'),
('Bósnia e Herzegovina', 'BIH', 'B'),
('Catar', 'QAT', 'B'),
('Suíça', 'SUI', 'B'),
-- Grupo C
('Brasil', 'BRA', 'C'),
('Marrocos', 'MAR', 'C'),
('Haiti', 'HAI', 'C'),
('Escócia', 'SCO', 'C'),
-- Grupo D
('Estados Unidos', 'USA', 'D'),
('Paraguai', 'PAR', 'D'),
('Austrália', 'AUS', 'D'),
('Turquia', 'TUR', 'D'),
-- Grupo E
('Alemanha', 'GER', 'E'),
('Curaçao', 'CUW', 'E'),
('Costa do Marfim', 'CIV', 'E'),
('Equador', 'ECU', 'E'),
-- Grupo F
('Holanda', 'NED', 'F'),
('Japão', 'JPN', 'F'),
('Suécia', 'SWE', 'F'),
('Tunísia', 'TUN', 'F'),
-- Grupo G
('Bélgica', 'BEL', 'G'),
('Egito', 'EGY', 'G'),
('Irã', 'IRN', 'G'),
('Nova Zelândia', 'NZL', 'G'),
-- Grupo H
('Espanha', 'ESP', 'H'),
('Cabo Verde', 'CPV', 'H'),
('Arábia Saudita', 'KSA', 'H'),
('Uruguai', 'URU', 'H'),
-- Grupo I
('França', 'FRA', 'I'),
('Senegal', 'SEN', 'I'),
('Iraque', 'IRQ', 'I'),
('Noruega', 'NOR', 'I'),
-- Grupo J
('Argentina', 'ARG', 'J'),
('Argélia', 'ALG', 'J'),
('Áustria', 'AUT', 'J'),
('Jordânia', 'JOR', 'J'),
-- Grupo K
('Portugal', 'POR', 'K'),
('RD Congo', 'COD', 'K'),
('Uzbequistão', 'UZB', 'K'),
('Colômbia', 'COL', 'K'),
-- Grupo L
('Inglaterra', 'ENG', 'L'),
('Croácia', 'CRO', 'L'),
('Gana', 'GHA', 'L'),
('Panamá', 'PAN', 'L');

-- ── ADMIN PADRÃO ──────────────────────────────
-- Senha: admin123 (BCrypt hash)
INSERT INTO worldcup.usuario (nome, email, senha_hash, role) VALUES
('Admin', 'admin@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');
