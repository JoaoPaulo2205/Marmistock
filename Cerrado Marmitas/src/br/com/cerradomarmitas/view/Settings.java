package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Configuracao;
import br.com.cerradomarmitas.util.Aparencia;

import javax.swing.*;
import java.awt.*;

public class Settings {
    private final JPanel jpanel = new JPanel(new BorderLayout(8, 8));
    private final JComboBox<String> temaComboBox = new JComboBox<>(new String[]{"Claro", "Escuro"});
    private final JComboBox<String> fonteComboBox = new JComboBox<>(new String[]{"Padrão", "Grande"});

    public Settings() {
        criarTela();
        preencher();
    }

    private void criarTela() {
        jpanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel dados = new JPanel(new GridBagLayout());
        dados.setBorder(BorderFactory.createTitledBorder("Configurações"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        adicionar(dados, gbc, "Tema:", temaComboBox, 0);
        adicionar(dados, gbc, "Fonte do sistema:", fonteComboBox, 1);
        jpanel.add(dados, BorderLayout.CENTER);

        JButton cancelarButton = new JButton("Cancelar");
        JButton salvarButton = new JButton("Salvar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(cancelarButton);
        botoes.add(salvarButton);
        jpanel.add(botoes, BorderLayout.SOUTH);
        cancelarButton.addActionListener(e -> fechar());
        salvarButton.addActionListener(e -> salvar());
    }

    private void adicionar(JPanel panel, GridBagConstraints gbc, String texto, JComponent campo, int linha) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.weightx = 0;
        panel.add(new JLabel(texto), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void preencher() {
        Configuracao configuracao = Aparencia.carregar();
        temaComboBox.setSelectedItem(configuracao.getTema());
        fonteComboBox.setSelectedItem(configuracao.getFontesSistema());
    }

    private void salvar() {
        Configuracao configuracao = new Configuracao(1,
                (String) temaComboBox.getSelectedItem(),
                (String) fonteComboBox.getSelectedItem());
        Aparencia.salvar(configuracao);
        JOptionPane.showMessageDialog(jpanel, "Configurações salvas com sucesso.");
        fechar();
    }

    private void fechar() {
        Window window = SwingUtilities.getWindowAncestor(jpanel);
        if (window != null) {
            window.dispose();
        }
    }

    public JPanel getPainel() {
        return jpanel;
    }
}
