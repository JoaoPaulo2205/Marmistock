-- PostgreSQL
-- Rode depois de 01_criar_banco.sql:
-- psql -U postgres -d trabalho_banco_dados -f 02_popular_marmitas_2500.sql
--
-- Este script limpa as tabelas do modelo e recria uma massa de teste com:
-- - 10 categorias
-- - 30 receitas
-- - 40 ingredientes
-- - 180 lotes
-- - 12 locais de armazenamento
-- - 60 consumidores
-- - 2500 marmitas fisicas
-- - movimentacoes para as marmitas consumidas e descartadas

BEGIN;

TRUNCATE TABLE
    registro_descarte,
    registro_consumo,
    registro_movimentacao,
    marmita_fisica,
    lote,
    receita_ingrediente,
    receita,
    ingrediente,
    categoria,
    local_armazenamento,
    consumidor
RESTART IDENTITY CASCADE;

INSERT INTO categoria (nome, descricao) VALUES
    ('Tradicional', 'Marmitas com combinacoes classicas do dia a dia.'),
    ('Fitness', 'Receitas com foco em proteina magra, legumes e carboidratos equilibrados.'),
    ('Vegetariana', 'Pratos sem carne com leguminosas, ovos, queijos e vegetais.'),
    ('Vegana', 'Pratos sem ingredientes de origem animal.'),
    ('Low Carb', 'Receitas com menor proporcao de carboidratos.'),
    ('Executiva', 'Marmitas completas com combinacoes variadas.'),
    ('Infantil', 'Porcoes e temperos pensados para criancas.'),
    ('Especial', 'Receitas sazonais ou de maior valor agregado.'),
    ('Sopas e Caldos', 'Preparos liquidos ou cremosos congelados.'),
    ('Massas', 'Pratos a base de massas e molhos.');

INSERT INTO ingrediente (nome, unidade_medida) VALUES
    ('Arroz branco', 'g'),
    ('Arroz integral', 'g'),
    ('Feijao carioca', 'g'),
    ('Feijao preto', 'g'),
    ('Lentilha', 'g'),
    ('Grao-de-bico', 'g'),
    ('Macarrao', 'g'),
    ('Batata doce', 'g'),
    ('Mandioca', 'g'),
    ('Quinoa', 'g'),
    ('Frango em cubos', 'g'),
    ('Carne bovina', 'g'),
    ('Carne moida', 'g'),
    ('Peixe', 'g'),
    ('Ovo', 'un'),
    ('Tofu', 'g'),
    ('Proteina de soja', 'g'),
    ('Queijo branco', 'g'),
    ('Brocolis', 'g'),
    ('Cenoura', 'g'),
    ('Abobrinha', 'g'),
    ('Couve-flor', 'g'),
    ('Ervilha', 'g'),
    ('Milho', 'g'),
    ('Tomate', 'g'),
    ('Cebola', 'g'),
    ('Alho', 'g'),
    ('Molho de tomate', 'ml'),
    ('Creme de leite', 'ml'),
    ('Azeite', 'ml'),
    ('Sal', 'g'),
    ('Pimenta', 'g'),
    ('Cheiro-verde', 'g'),
    ('Espinafre', 'g'),
    ('Berinjela', 'g'),
    ('Palmito', 'g'),
    ('Cogumelo', 'g'),
    ('Leite de coco', 'ml'),
    ('Calabresa', 'g'),
    ('Abobora', 'g');

INSERT INTO local_armazenamento (nome, descricao)
SELECT
    'Freezer ' || freezer || ' - Gaveta ' || gaveta,
    'Setor congelado ' || freezer || ', gaveta ' || gaveta || '.'
FROM generate_series(1, 3) AS freezer
CROSS JOIN generate_series(1, 4) AS gaveta;

INSERT INTO consumidor (nome)
SELECT 'Consumidor ' || LPAD(gs::TEXT, 3, '0')
FROM generate_series(1, 60) AS gs;

INSERT INTO receita (nome, descricao, modo_preparo, id_categoria) VALUES
    ('Frango com arroz e feijao', 'Marmita tradicional com frango grelhado, arroz e feijao.', 'Preparar arroz e feijao. Grelhar o frango, montar a porcao e resfriar antes de congelar.', 1),
    ('Carne moida com pure de batata doce', 'Carne moida refogada acompanhada de pure de batata doce.', 'Refogar a carne com temperos, cozinhar a batata doce, amassar e montar a marmita.', 1),
    ('Peixe com legumes', 'File de peixe com legumes cozidos no vapor.', 'Assar o peixe, cozinhar os legumes e porcionar.', 2),
    ('Frango fit com quinoa', 'Frango em cubos com quinoa e brocolis.', 'Cozinhar a quinoa, grelhar o frango e finalizar com brocolis.', 2),
    ('Omelete com arroz integral', 'Omelete assado com arroz integral e vegetais.', 'Assar os ovos com legumes, cozinhar o arroz integral e montar.', 2),
    ('Lentilha com legumes', 'Ensopado de lentilha com cenoura e abobrinha.', 'Cozinhar a lentilha com legumes e reduzir o caldo.', 3),
    ('Grao-de-bico com arroz integral', 'Grao-de-bico temperado com arroz integral.', 'Cozinhar grao-de-bico, refogar com temperos e servir com arroz integral.', 3),
    ('Escondidinho vegetariano', 'Escondidinho de legumes com queijo branco.', 'Preparar recheio de legumes, cobrir com pure e gratinar.', 3),
    ('Tofu oriental com legumes', 'Tofu grelhado com legumes salteados.', 'Grelhar tofu, saltear legumes e montar.', 4),
    ('Proteina de soja ao molho', 'Proteina de soja cozida em molho de tomate.', 'Hidratar a proteina, refogar com molho e porcionar.', 4),
    ('Curry vegano de grao-de-bico', 'Grao-de-bico ao curry com leite de coco.', 'Cozinhar o grao-de-bico no leite de coco com temperos.', 4),
    ('Carne com couve-flor', 'Carne bovina com couve-flor e abobrinha.', 'Grelhar a carne, cozinhar os vegetais e montar.', 5),
    ('Frango low carb', 'Frango com brocolis, couve-flor e azeite.', 'Grelhar frango e legumes, finalizar com azeite.', 5),
    ('Ovos com espinafre', 'Ovos mexidos com espinafre e cogumelos.', 'Preparar ovos mexidos com espinafre e cogumelos salteados.', 5),
    ('Marmita executiva de carne', 'Carne bovina com arroz, feijao e legumes.', 'Preparar acompanhamentos, grelhar carne e montar porcoes completas.', 6),
    ('Marmita executiva de frango', 'Frango com arroz, feijao e legumes.', 'Preparar acompanhamentos, grelhar frango e montar porcoes completas.', 6),
    ('Marmita executiva vegetariana', 'Combinacao vegetariana completa com leguminosas.', 'Cozinhar leguminosas, vegetais e carboidrato.', 6),
    ('Mini frango colorido', 'Porcao infantil com frango, arroz e cenoura.', 'Preparar com pouco sal, cortar em tamanhos pequenos e porcionar.', 7),
    ('Mini macarrao ao sugo', 'Porcao infantil de macarrao com molho de tomate.', 'Cozinhar macarrao, preparar molho simples e porcionar.', 7),
    ('Mini escondidinho de carne', 'Escondidinho infantil de carne moida.', 'Preparar carne moida suave, cobrir com pure e assar.', 7),
    ('Carne especial com cogumelos', 'Carne bovina com cogumelos e molho encorpado.', 'Selar a carne, preparar molho com cogumelos e montar.', 8),
    ('Risoto de palmito', 'Receita cremosa de arroz com palmito.', 'Cozinhar arroz, incorporar palmito e finalizar cremoso.', 8),
    ('Frango ao creme', 'Frango em cubos com molho cremoso.', 'Grelhar frango, adicionar creme e ajustar temperos.', 8),
    ('Caldo de abobora', 'Caldo cremoso de abobora.', 'Cozinhar abobora com temperos e bater ate ficar cremoso.', 9),
    ('Sopa de legumes', 'Sopa leve de legumes variados.', 'Cozinhar legumes, ajustar caldo e porcionar.', 9),
    ('Caldo verde adaptado', 'Caldo com batata, couve e calabresa.', 'Cozinhar base, adicionar couve e calabresa.', 9),
    ('Macarrao a bolonhesa', 'Macarrao com molho de carne moida.', 'Cozinhar massa, preparar molho bolonhesa e montar.', 10),
    ('Macarrao com frango', 'Massa com frango e molho de tomate.', 'Cozinhar massa, preparar frango ao molho e montar.', 10),
    ('Lasanha de berinjela', 'Camadas de berinjela com molho e queijo.', 'Grelhar berinjela, montar camadas e assar.', 10),
    ('Nhoque ao sugo', 'Nhoque com molho de tomate simples.', 'Cozinhar nhoque, preparar molho e porcionar.', 10);

INSERT INTO receita_ingrediente (id_receita, id_ingrediente, quantidade)
SELECT
    r.id_receita,
    i.id_ingrediente,
    ROUND((40 + ((r.id_receita * 17 + i.id_ingrediente * 11) % 260))::NUMERIC, 2) AS quantidade
FROM receita r
JOIN ingrediente i
    ON i.id_ingrediente IN (
        ((r.id_receita * 3 - 2) % 40) + 1,
        ((r.id_receita * 3 - 1) % 40) + 1,
        ((r.id_receita * 3) % 40) + 1,
        ((r.id_receita * 3 + 1) % 40) + 1,
        ((r.id_receita * 3 + 2) % 40) + 1
    );

INSERT INTO lote (data_preparo, data_validade, id_receita)
SELECT
    CURRENT_DATE - ((gs % 90)::INT),
    CURRENT_DATE - ((gs % 90)::INT) + 120,
    ((gs - 1) % 30) + 1
FROM generate_series(1, 180) AS gs;

INSERT INTO marmita_fisica (status, id_lote, id_local)
SELECT
    CASE
        WHEN gs % 20 = 0 THEN 'DESCARTADA'
        WHEN gs % 4 = 0 THEN 'CONSUMIDA'
        ELSE 'DISPONIVEL'
    END AS status,
    ((gs - 1) % 180) + 1 AS id_lote,
    ((gs - 1) % 12) + 1 AS id_local
FROM generate_series(1, 2500) AS gs;

WITH mov_consumo AS (
    INSERT INTO registro_movimentacao (data_movimentacao, tipo, id_marmita)
    SELECT
        l.data_preparo + ((m.id_marmita % 45) + 1),
        'consumo',
        m.id_marmita
    FROM marmita_fisica m
    JOIN lote l ON l.id_lote = m.id_lote
    WHERE m.status = 'CONSUMIDA'
    RETURNING id_movimentacao, id_marmita
)
INSERT INTO registro_consumo (id_movimentacao, id_consumidor)
SELECT
    id_movimentacao,
    ((id_marmita - 1) % 60) + 1
FROM mov_consumo;

WITH mov_descarte AS (
    INSERT INTO registro_movimentacao (data_movimentacao, tipo, id_marmita)
    SELECT
        l.data_preparo + ((m.id_marmita % 70) + 10),
        'descarte',
        m.id_marmita
    FROM marmita_fisica m
    JOIN lote l ON l.id_lote = m.id_lote
    WHERE m.status = 'DESCARTADA'
    RETURNING id_movimentacao, id_marmita
)
INSERT INTO registro_descarte (id_movimentacao, motivo)
SELECT
    id_movimentacao,
    CASE
        WHEN id_marmita % 3 = 0 THEN 'Validade expirada'
        WHEN id_marmita % 3 = 1 THEN 'Embalagem danificada'
        ELSE 'Alteracao de aspecto apos armazenamento'
    END
FROM mov_descarte;

COMMIT;

SELECT 'categoria' AS tabela, COUNT(*) AS total FROM categoria
UNION ALL SELECT 'receita', COUNT(*) FROM receita
UNION ALL SELECT 'ingrediente', COUNT(*) FROM ingrediente
UNION ALL SELECT 'receita_ingrediente', COUNT(*) FROM receita_ingrediente
UNION ALL SELECT 'lote', COUNT(*) FROM lote
UNION ALL SELECT 'local_armazenamento', COUNT(*) FROM local_armazenamento
UNION ALL SELECT 'consumidor', COUNT(*) FROM consumidor
UNION ALL SELECT 'marmita_fisica', COUNT(*) FROM marmita_fisica
UNION ALL SELECT 'registro_movimentacao', COUNT(*) FROM registro_movimentacao
UNION ALL SELECT 'registro_consumo', COUNT(*) FROM registro_consumo
UNION ALL SELECT 'registro_descarte', COUNT(*) FROM registro_descarte
ORDER BY tabela;

SELECT status, COUNT(*) AS total
FROM marmita_fisica
GROUP BY status
ORDER BY status;
