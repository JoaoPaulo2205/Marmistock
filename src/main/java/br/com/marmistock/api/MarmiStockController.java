package br.com.marmistock.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MarmiStockController {
    private final JdbcTemplate jdbc;

    public MarmiStockController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/dashboard")
    Map<String, Object> dashboard() {
        Integer total = count("SELECT COUNT(*) FROM marmita_fisica");
        Integer disponiveis = count("SELECT COUNT(*) FROM marmita_fisica WHERE status = 'DISPONIVEL'");
        Integer vencidas = count("""
                SELECT COUNT(*) FROM marmita_fisica m
                JOIN lote l ON l.id_lote = m.id_lote
                WHERE m.status = 'DISPONIVEL' AND l.data_validade < CURRENT_DATE
                """);
        Integer proximas = count("""
                SELECT COUNT(*) FROM marmita_fisica m
                JOIN lote l ON l.id_lote = m.id_lote
                WHERE m.status = 'DISPONIVEL' AND l.data_validade BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
                """);
        Integer consumidas = count("SELECT COUNT(*) FROM marmita_fisica WHERE status = 'CONSUMIDA'");
        Integer descartadas = count("SELECT COUNT(*) FROM marmita_fisica WHERE status = 'DESCARTADA'");

        List<Map<String, Object>> porCategoria = jdbc.queryForList("""
                SELECT c.nome, COUNT(m.id_marmita) AS total
                FROM categoria c
                LEFT JOIN receita r ON r.id_categoria = c.id_categoria
                LEFT JOIN lote l ON l.id_receita = r.id_receita
                LEFT JOIN marmita_fisica m ON m.id_lote = l.id_lote AND m.status = 'DISPONIVEL'
                GROUP BY c.nome
                ORDER BY total DESC, c.nome
                """);
        List<Map<String, Object>> recentes = jdbc.queryForList("""
                SELECT rm.id_movimentacao, rm.data_movimentacao, rm.tipo, mf.id_marmita, r.nome AS receita
                FROM registro_movimentacao rm
                JOIN marmita_fisica mf ON mf.id_marmita = rm.id_marmita
                JOIN lote l ON l.id_lote = mf.id_lote
                JOIN receita r ON r.id_receita = l.id_receita
                ORDER BY rm.data_movimentacao DESC, rm.id_movimentacao DESC
                LIMIT 8
                """);

        return Map.of(
                "total", total,
                "disponiveis", disponiveis,
                "vencidas", vencidas,
                "proximas", proximas,
                "consumidas", consumidas,
                "descartadas", descartadas,
                "porCategoria", porCategoria,
                "recentes", recentes
        );
    }

    @GetMapping("/categorias")
    List<Map<String, Object>> categorias() {
        return jdbc.queryForList("SELECT id_categoria AS id, nome, descricao FROM categoria ORDER BY nome");
    }

    @PostMapping("/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> criarCategoria(@Valid @RequestBody NomeDescricao body) {
        Number id = insert("INSERT INTO categoria (nome, descricao) VALUES (?, ?)", body.nome(), body.descricao());
        return Map.of("id", id);
    }

    @PutMapping("/categorias/{id}")
    void atualizarCategoria(@PathVariable int id, @Valid @RequestBody NomeDescricao body) {
        jdbc.update("UPDATE categoria SET nome = ?, descricao = ? WHERE id_categoria = ?", body.nome(), body.descricao(), id);
    }

    @DeleteMapping("/categorias/{id}")
    void excluirCategoria(@PathVariable int id) {
        jdbc.update("DELETE FROM categoria WHERE id_categoria = ?", id);
    }

    @GetMapping("/ingredientes")
    List<Map<String, Object>> ingredientes() {
        return jdbc.queryForList("SELECT id_ingrediente AS id, nome, unidade_medida FROM ingrediente ORDER BY nome");
    }

    @PostMapping("/ingredientes")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> criarIngrediente(@Valid @RequestBody Ingrediente body) {
        Number id = insert("INSERT INTO ingrediente (nome, unidade_medida) VALUES (?, ?)", body.nome(), body.unidadeMedida());
        return Map.of("id", id);
    }

    @PutMapping("/ingredientes/{id}")
    void atualizarIngrediente(@PathVariable int id, @Valid @RequestBody Ingrediente body) {
        jdbc.update("UPDATE ingrediente SET nome = ?, unidade_medida = ? WHERE id_ingrediente = ?", body.nome(), body.unidadeMedida(), id);
    }

    @DeleteMapping("/ingredientes/{id}")
    void excluirIngrediente(@PathVariable int id) {
        jdbc.update("DELETE FROM ingrediente WHERE id_ingrediente = ?", id);
    }

    @GetMapping("/receitas")
    List<Map<String, Object>> receitas() {
        return jdbc.queryForList("""
                SELECT r.id_receita AS id, r.nome, r.descricao, r.modo_preparo, r.id_categoria, c.nome AS categoria
                FROM receita r
                JOIN categoria c ON c.id_categoria = r.id_categoria
                ORDER BY r.nome
                """);
    }

    @PostMapping("/receitas")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Map<String, Object> criarReceita(@Valid @RequestBody Receita body) {
        Number id = insert("INSERT INTO receita (nome, descricao, modo_preparo, id_categoria) VALUES (?, ?, ?, ?)",
                body.nome(), body.descricao(), body.modoPreparo(), body.idCategoria());
        salvarIngredientesReceita(id.intValue(), body.ingredientes());
        return Map.of("id", id);
    }

    @PutMapping("/receitas/{id}")
    @Transactional
    void atualizarReceita(@PathVariable int id, @Valid @RequestBody Receita body) {
        jdbc.update("UPDATE receita SET nome = ?, descricao = ?, modo_preparo = ?, id_categoria = ? WHERE id_receita = ?",
                body.nome(), body.descricao(), body.modoPreparo(), body.idCategoria(), id);
        jdbc.update("DELETE FROM receita_ingrediente WHERE id_receita = ?", id);
        salvarIngredientesReceita(id, body.ingredientes());
    }

    @DeleteMapping("/receitas/{id}")
    void excluirReceita(@PathVariable int id) {
        jdbc.update("DELETE FROM receita WHERE id_receita = ?", id);
    }

    @GetMapping("/receitas/{id}/ingredientes")
    List<Map<String, Object>> ingredientesReceita(@PathVariable int id) {
        return jdbc.queryForList("""
                SELECT i.id_ingrediente AS id, i.nome, i.unidade_medida, ri.quantidade
                FROM receita_ingrediente ri
                JOIN ingrediente i ON i.id_ingrediente = ri.id_ingrediente
                WHERE ri.id_receita = ?
                ORDER BY i.nome
                """, id);
    }

    @GetMapping("/lotes")
    List<Map<String, Object>> lotes() {
        return jdbc.queryForList("""
                SELECT l.id_lote AS id, l.data_preparo, l.data_validade, l.id_receita, r.nome AS receita,
                       COUNT(m.id_marmita) AS marmitas
                FROM lote l
                JOIN receita r ON r.id_receita = l.id_receita
                LEFT JOIN marmita_fisica m ON m.id_lote = l.id_lote
                GROUP BY l.id_lote, r.nome
                ORDER BY l.data_validade, l.id_lote DESC
                """);
    }

    @PostMapping("/lotes")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> criarLote(@Valid @RequestBody Lote body) {
        Number id = insert("INSERT INTO lote (data_preparo, data_validade, id_receita) VALUES (?, ?, ?)",
                Date.valueOf(body.dataPreparo()), Date.valueOf(body.dataValidade()), body.idReceita());
        return Map.of("id", id);
    }

    @PutMapping("/lotes/{id}")
    void atualizarLote(@PathVariable int id, @Valid @RequestBody Lote body) {
        jdbc.update("UPDATE lote SET data_preparo = ?, data_validade = ?, id_receita = ? WHERE id_lote = ?",
                Date.valueOf(body.dataPreparo()), Date.valueOf(body.dataValidade()), body.idReceita(), id);
    }

    @DeleteMapping("/lotes/{id}")
    void excluirLote(@PathVariable int id) {
        jdbc.update("DELETE FROM lote WHERE id_lote = ?", id);
    }

    @GetMapping("/locais")
    List<Map<String, Object>> locais() {
        return jdbc.queryForList("SELECT id_local AS id, nome, descricao FROM local_armazenamento ORDER BY nome");
    }

    @PostMapping("/locais")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> criarLocal(@Valid @RequestBody NomeDescricao body) {
        Number id = insert("INSERT INTO local_armazenamento (nome, descricao) VALUES (?, ?)", body.nome(), body.descricao());
        return Map.of("id", id);
    }

    @PutMapping("/locais/{id}")
    void atualizarLocal(@PathVariable int id, @Valid @RequestBody NomeDescricao body) {
        jdbc.update("UPDATE local_armazenamento SET nome = ?, descricao = ? WHERE id_local = ?", body.nome(), body.descricao(), id);
    }

    @DeleteMapping("/locais/{id}")
    void excluirLocal(@PathVariable int id) {
        jdbc.update("DELETE FROM local_armazenamento WHERE id_local = ?", id);
    }

    @GetMapping("/consumidores")
    List<Map<String, Object>> consumidores() {
        return jdbc.queryForList("SELECT id_consumidor AS id, nome FROM consumidor ORDER BY nome");
    }

    @PostMapping("/consumidores")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> criarConsumidor(@Valid @RequestBody Nome body) {
        Number id = insert("INSERT INTO consumidor (nome) VALUES (?)", body.nome());
        return Map.of("id", id);
    }

    @PutMapping("/consumidores/{id}")
    void atualizarConsumidor(@PathVariable int id, @Valid @RequestBody Nome body) {
        jdbc.update("UPDATE consumidor SET nome = ? WHERE id_consumidor = ?", body.nome(), id);
    }

    @DeleteMapping("/consumidores/{id}")
    void excluirConsumidor(@PathVariable int id) {
        jdbc.update("DELETE FROM consumidor WHERE id_consumidor = ?", id);
    }

    @GetMapping("/marmitas")
    List<Map<String, Object>> marmitas() {
        return jdbc.queryForList("""
                SELECT m.id_marmita AS id, m.status, m.id_lote, m.id_local, la.nome AS local,
                       l.data_preparo, l.data_validade, r.nome AS receita, c.nome AS categoria,
                       CASE
                         WHEN m.status <> 'DISPONIVEL' THEN m.status
                         WHEN l.data_validade < CURRENT_DATE THEN 'VENCIDA'
                         WHEN l.data_validade <= CURRENT_DATE + INTERVAL '7 days' THEN 'PROXIMA'
                         ELSE 'OK'
                       END AS situacao
                FROM marmita_fisica m
                JOIN lote l ON l.id_lote = m.id_lote
                JOIN receita r ON r.id_receita = l.id_receita
                JOIN categoria c ON c.id_categoria = r.id_categoria
                JOIN local_armazenamento la ON la.id_local = m.id_local
                ORDER BY l.data_validade, m.id_marmita DESC
                """);
    }

    @PostMapping("/marmitas")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> criarMarmita(@Valid @RequestBody Marmita body) {
        Number id = insert("INSERT INTO marmita_fisica (status, id_lote, id_local) VALUES (?, ?, ?)",
                status(body.status()), body.idLote(), body.idLocal());
        return Map.of("id", id);
    }

    @PutMapping("/marmitas/{id}")
    void atualizarMarmita(@PathVariable int id, @Valid @RequestBody Marmita body) {
        jdbc.update("UPDATE marmita_fisica SET status = ?, id_lote = ?, id_local = ? WHERE id_marmita = ?",
                status(body.status()), body.idLote(), body.idLocal(), id);
    }

    @DeleteMapping("/marmitas/{id}")
    void excluirMarmita(@PathVariable int id) {
        jdbc.update("DELETE FROM marmita_fisica WHERE id_marmita = ?", id);
    }

    @PostMapping("/marmitas/{id}/consumir")
    @Transactional
    void consumir(@PathVariable int id, @Valid @RequestBody Consumo body) {
        validarMarmitaDisponivel(id);
        jdbc.update("UPDATE marmita_fisica SET status = 'CONSUMIDA' WHERE id_marmita = ?", id);
        Number mov = insert("INSERT INTO registro_movimentacao (data_movimentacao, tipo, id_marmita) VALUES (?, 'consumo', ?)",
                Date.valueOf(body.dataMovimentacao()), id);
        jdbc.update("INSERT INTO registro_consumo (id_movimentacao, id_consumidor) VALUES (?, ?)", mov.intValue(), body.idConsumidor());
    }

    @PostMapping("/marmitas/{id}/descartar")
    @Transactional
    void descartar(@PathVariable int id, @Valid @RequestBody Descarte body) {
        validarMarmitaDisponivel(id);
        jdbc.update("UPDATE marmita_fisica SET status = 'DESCARTADA' WHERE id_marmita = ?", id);
        Number mov = insert("INSERT INTO registro_movimentacao (data_movimentacao, tipo, id_marmita) VALUES (?, 'descarte', ?)",
                Date.valueOf(body.dataMovimentacao()), id);
        jdbc.update("INSERT INTO registro_descarte (id_movimentacao, motivo) VALUES (?, ?)", mov.intValue(), body.motivo());
    }

    private Integer count(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }

    private Number insert(String sql, Object... params) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, holder);
        Map<String, Object> keys = holder.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException("Insert nao retornou chave gerada.");
        }
        return keys.values().stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Insert retornou chave gerada vazia."));
    }

    private void salvarIngredientesReceita(int idReceita, List<ReceitaIngrediente> ingredientes) {
        if (ingredientes == null) {
            return;
        }
        for (ReceitaIngrediente item : ingredientes) {
            jdbc.update("INSERT INTO receita_ingrediente (id_receita, id_ingrediente, quantidade) VALUES (?, ?, ?)",
                    idReceita, item.idIngrediente(), item.quantidade());
        }
    }

    private String status(String value) {
        if (value == null || value.isBlank()) {
            return "DISPONIVEL";
        }
        String normalized = value.toUpperCase();
        if (!List.of("DISPONIVEL", "CONSUMIDA", "DESCARTADA").contains(normalized)) {
            throw new IllegalArgumentException("Status deve ser DISPONIVEL, CONSUMIDA ou DESCARTADA.");
        }
        return normalized;
    }

    private void validarMarmitaDisponivel(int id) {
        List<String> statuses = jdbc.queryForList("SELECT status FROM marmita_fisica WHERE id_marmita = ?", String.class, id);
        if (statuses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Marmita nao encontrada.");
        }
        if (!"DISPONIVEL".equals(statuses.getFirst())) {
            throw new IllegalArgumentException("A marmita precisa estar DISPONIVEL para movimentacao.");
        }
    }

    public record Nome(@NotBlank String nome) {}

    public record NomeDescricao(@NotBlank String nome, String descricao) {}

    public record Ingrediente(@NotBlank String nome, @NotBlank String unidadeMedida) {}

    public record Receita(
            @NotBlank String nome,
            String descricao,
            String modoPreparo,
            @NotNull Integer idCategoria,
            List<ReceitaIngrediente> ingredientes
    ) {}

    public record ReceitaIngrediente(
            @NotNull Integer idIngrediente,
            @NotNull @DecimalMin("0.01") BigDecimal quantidade
    ) {}

    public record Lote(@NotNull LocalDate dataPreparo, @NotNull LocalDate dataValidade, @NotNull Integer idReceita) {}

    public record Marmita(String status, @NotNull Integer idLote, @NotNull Integer idLocal) {}

    public record Consumo(@NotNull LocalDate dataMovimentacao, @NotNull Integer idConsumidor) {}

    public record Descarte(@NotNull LocalDate dataMovimentacao, @NotBlank String motivo) {}
}
