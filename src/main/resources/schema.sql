CREATE TABLE IF NOT EXISTS categoria (
    id_categoria SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT
);

CREATE TABLE IF NOT EXISTS receita (
    id_receita SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    modo_preparo TEXT,
    id_categoria INT NOT NULL REFERENCES categoria(id_categoria) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS ingrediente (
    id_ingrediente SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    unidade_medida VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS receita_ingrediente (
    id_receita INT NOT NULL REFERENCES receita(id_receita) ON DELETE CASCADE ON UPDATE CASCADE,
    id_ingrediente INT NOT NULL REFERENCES ingrediente(id_ingrediente) ON DELETE RESTRICT ON UPDATE CASCADE,
    quantidade DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_receita, id_ingrediente)
);

CREATE TABLE IF NOT EXISTS lote (
    id_lote SERIAL PRIMARY KEY,
    data_preparo DATE NOT NULL,
    data_validade DATE NOT NULL,
    id_receita INT NOT NULL REFERENCES receita(id_receita) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS local_armazenamento (
    id_local SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT
);

CREATE TABLE IF NOT EXISTS marmita_fisica (
    id_marmita SERIAL PRIMARY KEY,
    status VARCHAR(30) NOT NULL CHECK (status IN ('DISPONIVEL', 'CONSUMIDA', 'DESCARTADA')),
    id_lote INT NOT NULL REFERENCES lote(id_lote) ON DELETE RESTRICT ON UPDATE CASCADE,
    id_local INT NOT NULL REFERENCES local_armazenamento(id_local) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS consumidor (
    id_consumidor SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS registro_movimentacao (
    id_movimentacao SERIAL PRIMARY KEY,
    data_movimentacao DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('consumo', 'descarte')),
    id_marmita INT NOT NULL REFERENCES marmita_fisica(id_marmita) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS registro_consumo (
    id_movimentacao INT PRIMARY KEY REFERENCES registro_movimentacao(id_movimentacao) ON DELETE CASCADE ON UPDATE CASCADE,
    id_consumidor INT NOT NULL REFERENCES consumidor(id_consumidor) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS registro_descarte (
    id_movimentacao INT PRIMARY KEY REFERENCES registro_movimentacao(id_movimentacao) ON DELETE CASCADE ON UPDATE CASCADE,
    motivo VARCHAR(255) NOT NULL
);

INSERT INTO categoria (nome, descricao)
SELECT 'Proteicas', 'Receitas focadas em alta saciedade'
WHERE NOT EXISTS (SELECT 1 FROM categoria);

INSERT INTO categoria (nome, descricao)
SELECT 'Low carb', 'Opcoes com menor carga de carboidratos'
WHERE (SELECT COUNT(*) FROM categoria) = 1;

INSERT INTO ingrediente (nome, unidade_medida)
SELECT 'Arroz integral', 'g'
WHERE NOT EXISTS (SELECT 1 FROM ingrediente);

INSERT INTO ingrediente (nome, unidade_medida)
SELECT 'Frango desfiado', 'g'
WHERE (SELECT COUNT(*) FROM ingrediente) = 1;

INSERT INTO local_armazenamento (nome, descricao)
SELECT 'Gaveta 1', 'Congelador superior'
WHERE NOT EXISTS (SELECT 1 FROM local_armazenamento);

INSERT INTO consumidor (nome)
SELECT 'Casa'
WHERE NOT EXISTS (SELECT 1 FROM consumidor);
