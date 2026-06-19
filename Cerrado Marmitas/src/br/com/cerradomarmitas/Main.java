package br.com.cerradomarmitas;

import br.com.cerradomarmitas.view.Login;
import br.com.cerradomarmitas.view.Supply;
import br.com.cerradomarmitas.util.Aparencia;
import br.com.cerradomarmitas.util.LoginPersistente;
import br.com.cerradomarmitas.util.Sessao;
import br.com.cerradomarmitas.models.Usuario;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Aparencia.aplicar(Aparencia.carregar());
            Usuario usuarioLembrado = LoginPersistente.carregar();
            boolean autenticado = usuarioLembrado != null;
            JFrame frame = new JFrame(autenticado
                    ? "Estoque - Cerrado Marmitas"
                    : "Login - Cerrado Marmitas");
            if (autenticado) {
                Sessao.iniciar(usuarioLembrado);
                frame.setContentPane(new Supply().getPainel());
            } else {
                frame.setContentPane(new Login().getPainel());
            }
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(autenticado ? 1050 : 520, autenticado ? 650 : 450);
            frame.setMinimumSize(autenticado
                    ? new java.awt.Dimension(900, 550)
                    : new java.awt.Dimension(480, 400));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
