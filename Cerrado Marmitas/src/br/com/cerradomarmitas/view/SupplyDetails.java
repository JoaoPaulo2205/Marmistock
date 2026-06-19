package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Marmita;
import br.com.cerradomarmitas.util.AppData;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class SupplyDetails {
    private final JPanel jpanel = new JPanel(new BorderLayout(8, 8));

    public SupplyDetails(Marmita marmita) {
        jpanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel dados = new JPanel(new GridBagLayout());
        dados.setBorder(BorderFactory.createTitledBorder("Detalhes da Marmita"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        adicionar(dados, gbc, "ID:", String.valueOf(marmita.getId()), 0);
        adicionar(dados, gbc, "Nome:", marmita.getNome(), 1);
        adicionar(dados, gbc, "Categoria:", marmita.getCategoria(), 2);
        adicionar(dados, gbc, "Quantidade:", String.valueOf(marmita.getQuantidade()), 3);
        adicionar(dados, gbc, "Valor:", String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", marmita.getValor()), 4);
        adicionar(dados, gbc, "Validade:", marmita.getDataValidade().format(AppData.DATE_FORMAT), 5);
        adicionar(dados, gbc, "Status:", Supply.statusDa(marmita.getDataValidade()), 6);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0;
        dados.add(new JLabel("Observações:"), gbc);
        JTextArea observacoes = new JTextArea(marmita.getObservacoes(), 5, 28);
        observacoes.setEditable(false);
        observacoes.setLineWrap(true);
        observacoes.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        dados.add(new JScrollPane(observacoes), gbc);
        jpanel.add(dados, BorderLayout.CENTER);

        JButton fecharButton = new JButton("Fechar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(fecharButton);
        jpanel.add(botoes, BorderLayout.SOUTH);
        fecharButton.addActionListener(e -> fechar());
    }

    private void adicionar(JPanel panel, GridBagConstraints gbc, String titulo, String valor, int linha) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.weightx = 0;
        panel.add(new JLabel(titulo), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(new JLabel(valor), gbc);
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
