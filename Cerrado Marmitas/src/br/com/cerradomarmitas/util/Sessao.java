package br.com.cerradomarmitas.util;

import br.com.cerradomarmitas.models.Usuario;

public final class Sessao {
    private static Usuario usuario;

    private Sessao() {
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void iniciar(Usuario usuarioAutenticado) {
        usuario = usuarioAutenticado;
    }

    public static void encerrar() {
        usuario = null;
    }
}
