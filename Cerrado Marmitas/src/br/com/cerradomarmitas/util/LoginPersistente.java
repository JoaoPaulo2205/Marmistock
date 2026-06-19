package br.com.cerradomarmitas.util;

import br.com.cerradomarmitas.models.Usuario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LoginPersistente {
    private static final Path ARQUIVO = Paths.get("data/sessao.csv");

    private LoginPersistente() {
    }

    public static void salvar(Usuario usuario) {
        try {
            Files.createDirectories(ARQUIVO.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(ARQUIVO, StandardCharsets.UTF_8)) {
                writer.write("usuarioId");
                writer.newLine();
                writer.write(String.valueOf(usuario.getId()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível salvar a preferência de login.", e);
        }
    }

    public static Usuario carregar() {
        if (Files.notExists(ARQUIVO)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(ARQUIVO, StandardCharsets.UTF_8)) {
            reader.readLine();
            String id = reader.readLine();
            if (Validador.campoVazio(id)) {
                return null;
            }
            Usuario usuario = AppData.usuarios().buscarPorId(Integer.parseInt(id.trim()));
            if (usuario == null) {
                limpar();
            }
            return usuario;
        } catch (IOException | NumberFormatException e) {
            limpar();
            return null;
        }
    }

    public static void limpar() {
        try {
            Files.deleteIfExists(ARQUIVO);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível remover a preferência de login.", e);
        }
    }
}
