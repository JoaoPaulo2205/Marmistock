package br.com.cerradomarmitas.util;

import br.com.cerradomarmitas.models.Configuracao;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Collections;

public final class Aparencia {
    private Aparencia() {
    }

    public static Configuracao carregar() {
        return AppData.configuracoes().carregarTodos().stream()
                .findFirst()
                .orElse(new Configuracao(1, "Claro", "Padrão"));
    }

    public static void aplicar(Configuracao configuracao) {
        try {
            if ("Escuro".equals(configuracao.getTema())) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
            int tamanho = "Grande".equals(configuracao.getFontesSistema()) ? 16 : 13;
            UIManager.put("defaultFont", new FontUIResource("SansSerif", Font.PLAIN, tamanho));
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
                window.invalidate();
                window.validate();
                window.repaint();
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException("Não foi possível aplicar a aparência.", e);
        }
    }

    public static void salvar(Configuracao configuracao) {
        AppData.configuracoes().salvarTodos(Collections.singletonList(configuracao));
        aplicar(configuracao);
    }
}
