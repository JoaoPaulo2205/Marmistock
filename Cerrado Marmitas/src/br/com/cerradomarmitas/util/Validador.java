package br.com.cerradomarmitas.util;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class Validador {
    private Validador() {
    }

    public static boolean campoVazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static boolean temPontoVirgula(String... campos) {
        for (String campo : campos) {
            if (campo != null && campo.contains(";")) {
                return true;
            }
        }
        return false;
    }

    public static int validarInteiro(String texto) {
        int valor = Integer.parseInt(texto.trim());
        if (valor < 0) {
            throw new NumberFormatException("O valor não pode ser negativo.");
        }
        return valor;
    }

    public static double validarDecimal(String texto) {
        double valor = Double.parseDouble(texto.trim().replace(',', '.'));
        if (!Double.isFinite(valor) || valor < 0) {
            throw new NumberFormatException("O valor deve ser um número positivo.");
        }
        return valor;
    }

    public static LocalDate validarData(String texto) {
        try {
            return LocalDate.parse(texto.trim(), AppData.DATE_FORMAT.withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Informe a validade no formato dd/MM/yyyy.");
        }
    }

    public static KeyAdapter bloqueadorPontoVirgula() {
        return new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ';') {
                    e.consume();
                }
            }
        };
    }
}
