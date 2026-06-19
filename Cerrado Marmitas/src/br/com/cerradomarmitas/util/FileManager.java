package br.com.cerradomarmitas.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class FileManager<T> {

    private final Path caminho;
    private final Function<String[], T> parser;
    private final Function<T, String[]> serializer;
    private String cabecalho;

    public FileManager(String caminhoArquivo, Function<String[], T> parser, Function<T, String[]> serializer) {
        this.caminho = Paths.get(caminhoArquivo);
        this.parser = parser;
        this.serializer = serializer;
    }

    public void setCabecalho(String cabecalho) {
        this.cabecalho = cabecalho;
        inicializarArquivo();
    }

    private void inicializarArquivo() {
        try {
            if (Files.notExists(caminho)) {
                if (caminho.getParent() != null) {
                    Files.createDirectories(caminho.getParent());
                }
                if (cabecalho != null) {
                    try (BufferedWriter writer = Files.newBufferedWriter(caminho, StandardCharsets.UTF_8)) {
                        writer.write(cabecalho);
                        writer.newLine();
                    }
                } else {
                     Files.createFile(caminho);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao inicializar arquivo: " + caminho, e);
        }
    }

    public List<T> carregarTodos() {
        if (Files.notExists(caminho)) return new ArrayList<>();
        List<T> resultados = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
            String linha;
            boolean primeiraLinha = true;
            while ((linha = reader.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue; // Pula o cabeçalho
                }
                if (!linha.trim().isEmpty()) {
                    resultados.add(parser.apply(linha.split(";", -1)));
                }
            }
            return resultados;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar registros: " + caminho, e);
        }
    }

    public void salvarTodos(List<T> registros) {
        inicializarArquivo();
        
        // Tenta recuperar cabeçalho se não estiver em memória
        if (cabecalho == null && Files.exists(caminho)) {
            try (BufferedReader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
                String primeiraLinha = reader.readLine();
                if (primeiraLinha != null && !primeiraLinha.trim().isEmpty()) {
                     cabecalho = primeiraLinha;
                }
            } catch (IOException e) {
                 // Ignore e segue
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(caminho, StandardCharsets.UTF_8)) {
            if (cabecalho != null) {
                writer.write(cabecalho);
                writer.newLine();
            }
            for (T r : registros) {
                writer.write(String.join(";", serializer.apply(r)));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar registros: " + caminho, e);
        }
    }

    public void adicionar(T registro) {
        inicializarArquivo();
        try (BufferedWriter writer = Files.newBufferedWriter(caminho, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(String.join(";", serializer.apply(registro)));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao adicionar registro: " + caminho, e);
        }
    }

    public boolean atualizar(Predicate<T> filtro, T novo) {
        List<T> registros = carregarTodos();
        boolean alterado = false;
        for (int i = 0; i < registros.size(); i++) {
            if (filtro.test(registros.get(i))) {
                registros.set(i, novo);
                alterado = true;
            }
        }
        if (alterado) salvarTodos(registros);
        return alterado;
    }

    public boolean remover(Predicate<T> filtro) {
        List<T> registros = carregarTodos();
        boolean removido = registros.removeIf(filtro);
        if (removido) salvarTodos(registros);
        return removido;
    }

    public List<T> buscar(Predicate<T> condicao) {
        return carregarTodos().stream()
                .filter(condicao)
                .collect(Collectors.toList());
    }

    public T buscarPorId(int id) {
        return carregarTodos().stream()
                .filter(r -> {
                    try {
                        return (int) r.getClass().getMethod("getId").invoke(r) == id;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);
    }

    public int proximoId() {
        List<T> registros = carregarTodos();
        if (registros.isEmpty()) return 1;
        int maxId = 0;
        for (T r : registros) {
            try {
                int id = (int) r.getClass().getMethod("getId").invoke(r);
                if (id > maxId) maxId = id;
            } catch (Exception e) {
                // Se não conseguir pegar ID, ignora ou lança erro
            }
        }
        return maxId + 1;
    }

    public long contar() {
        return carregarTodos().size();
    }
}
