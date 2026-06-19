package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Categoria;
import br.com.cerradomarmitas.models.Marmita;
import br.com.cerradomarmitas.util.AppData;
import br.com.cerradomarmitas.util.Sessao;
import br.com.cerradomarmitas.util.Validador;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.time.LocalDate;
import java.util.List;

public class SupplyRegister {
    private final JPanel jpanel = new JPanel(new BorderLayout(8, 8));
    private final JTextField nomeTextField = new JTextField(30);
    private final JComboBox<String> categoriaComboBox = new JComboBox<>();
    private final JTextField quantidadeTextField = new JTextField(10);
    private final JTextField valorTextField = new JTextField(10);
    private final JTextField validadeTextField = new JTextField(10);
    private final JTextArea observacoesTextArea = new JTextArea(5, 30);
    private final JLabel diasLabel = new JLabel("-");
    private final JLabel statusLabel = new JLabel("-");
    private final Marmita marmitaEmEdicao;
    private final Runnable aoSalvar;

    public SupplyRegister() {
        this(null, null);
    }

    public SupplyRegister(Marmita marmitaEmEdicao, Runnable aoSalvar) {
        this.marmitaEmEdicao = marmitaEmEdicao;
        this.aoSalvar = aoSalvar;
        criarTela();
        carregarCategorias();
        preencherFormulario();
    }

    private void criarTela() {
        jpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel dadosPanel = new JPanel(new GridBagLayout());
        dadosPanel.setBorder(BorderFactory.createTitledBorder(
                marmitaEmEdicao == null ? "Cadastrar Marmita" : "Editar Marmita"));
        jpanel.add(dadosPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        adicionarLinha(dadosPanel, gbc, "Nome da Marmita:", nomeTextField, 0);

        JPanel categoriaPanel = new JPanel(new BorderLayout(6, 0));
        JButton gerenciarCategoriasButton = new JButton("Gerenciar Categorias");
        categoriaPanel.add(categoriaComboBox, BorderLayout.CENTER);
        categoriaPanel.add(gerenciarCategoriasButton, BorderLayout.EAST);
        adicionarLinha(dadosPanel, gbc, "Categoria:", categoriaPanel, 1);

        adicionarLinha(dadosPanel, gbc, "Quantidade:", quantidadeTextField, 2);
        adicionarLinha(dadosPanel, gbc, "Valor (R$):", valorTextField, 3);
        adicionarLinha(dadosPanel, gbc, "Validade (dd/MM/yyyy):", validadeTextField, 4);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        gbc.weighty = 0;
        dadosPanel.add(new JLabel("Observações:"), gbc);

        observacoesTextArea.setLineWrap(true);
        observacoesTextArea.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        dadosPanel.add(new JScrollPane(observacoesTextArea), gbc);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informações"));
        infoPanel.add(new JLabel("Dias para vencer:"));
        infoPanel.add(diasLabel);
        infoPanel.add(new JLabel("Status:"));
        infoPanel.add(statusLabel);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dadosPanel.add(infoPanel, gbc);

        JButton salvarButton = new JButton("Salvar");
        JButton cancelarButton = new JButton("Cancelar");
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoesPanel.add(cancelarButton);
        botoesPanel.add(salvarButton);
        jpanel.add(botoesPanel, BorderLayout.SOUTH);

        KeyAdapter bloqueador = Validador.bloqueadorPontoVirgula();
        bloquearPontoVirgula(bloqueador, nomeTextField, quantidadeTextField,
                valorTextField, validadeTextField, observacoesTextArea);

        validadeTextField.addActionListener(e -> atualizarInformacoes());
        gerenciarCategoriasButton.addActionListener(e -> abrirCategorias());
        cancelarButton.addActionListener(e -> fecharJanela());
        salvarButton.addActionListener(e -> salvar());
    }

    private void adicionarLinha(JPanel panel, GridBagConstraints gbc, String label, Component campo, int linha) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void bloquearPontoVirgula(KeyAdapter bloqueador, JTextComponent... campos) {
        for (JTextComponent campo : campos) {
            campo.addKeyListener(bloqueador);
        }
    }

    private void carregarCategorias() {
        Object selecionada = categoriaComboBox.getSelectedItem();
        categoriaComboBox.removeAllItems();
        for (Categoria categoria : AppData.categorias().carregarTodos()) {
            categoriaComboBox.addItem(categoria.getNome());
        }
        if (selecionada != null) {
            categoriaComboBox.setSelectedItem(selecionada);
        }
    }

    private void preencherFormulario() {
        if (marmitaEmEdicao == null) {
            quantidadeTextField.setText("0");
            valorTextField.setText("0,00");
            validadeTextField.setText(LocalDate.now().format(AppData.DATE_FORMAT));
            atualizarInformacoes();
            return;
        }

        nomeTextField.setText(marmitaEmEdicao.getNome());
        categoriaComboBox.setSelectedItem(marmitaEmEdicao.getCategoria());
        quantidadeTextField.setText(String.valueOf(marmitaEmEdicao.getQuantidade()));
        valorTextField.setText(String.format("%.2f", marmitaEmEdicao.getValor()).replace('.', ','));
        validadeTextField.setText(marmitaEmEdicao.getDataValidade().format(AppData.DATE_FORMAT));
        observacoesTextArea.setText(marmitaEmEdicao.getObservacoes());
        atualizarInformacoes();
    }

    private void salvar() {
        try {
            String nome = nomeTextField.getText().trim();
            String categoria = (String) categoriaComboBox.getSelectedItem();
            String quantidadeTexto = quantidadeTextField.getText().trim();
            String valorTexto = valorTextField.getText().trim();
            String validadeTexto = validadeTextField.getText().trim();
            String observacoes = observacoesTextArea.getText().trim();

            if (Validador.campoVazio(nome) || Validador.campoVazio(categoria)
                    || Validador.campoVazio(quantidadeTexto) || Validador.campoVazio(valorTexto)
                    || Validador.campoVazio(validadeTexto)) {
                throw new IllegalArgumentException("Preencha todos os campos obrigatórios.");
            }
            if (Validador.temPontoVirgula(nome, categoria, quantidadeTexto, valorTexto,
                    validadeTexto, observacoes)) {
                throw new IllegalArgumentException("O caractere ; não é permitido.");
            }

            int quantidade = Validador.validarInteiro(quantidadeTexto);
            double valor = Validador.validarDecimal(valorTexto);
            LocalDate validade = Validador.validarData(validadeTexto);
            int id = marmitaEmEdicao == null ? AppData.marmitas().proximoId() : marmitaEmEdicao.getId();
            int usuarioId = marmitaEmEdicao == null
                    ? Sessao.getUsuario().getId()
                    : marmitaEmEdicao.getUsuarioId();
            Marmita marmita = new Marmita(id, usuarioId, nome, categoria, quantidade, valor, validade, observacoes);

            if (marmitaEmEdicao == null) {
                AppData.marmitas().adicionar(marmita);
            } else if (!AppData.marmitas().atualizar(
                    item -> item.getId() == id && item.getUsuarioId() == usuarioId, marmita)) {
                throw new IllegalStateException("A marmita não foi encontrada para edição.");
            }

            if (aoSalvar != null) {
                aoSalvar.run();
            }
            JOptionPane.showMessageDialog(jpanel,
                    marmitaEmEdicao == null ? "Marmita cadastrada com sucesso." : "Marmita atualizada com sucesso.");
            fecharJanela();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(jpanel, "Quantidade e valor devem ser números válidos.",
                    "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(jpanel, e.getMessage(), "Não foi possível salvar",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarInformacoes() {
        try {
            LocalDate validade = Validador.validarData(validadeTextField.getText());
            diasLabel.setText(String.valueOf(java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), validade)));
            statusLabel.setText(Supply.statusDa(validade));
        } catch (RuntimeException e) {
            diasLabel.setText("-");
            statusLabel.setText("Data inválida");
        }
    }

    private void abrirCategorias() {
        Window owner = SwingUtilities.getWindowAncestor(jpanel);
        Category category = new Category(this::carregarCategorias);
        JDialog dialog = new JDialog(owner, "Categorias - Cerrado Marmitas", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(category.getPainel());
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        carregarCategorias();
    }

    private void fecharJanela() {
        Window janela = SwingUtilities.getWindowAncestor(jpanel);
        if (janela != null) {
            janela.dispose();
        }
    }

    public JPanel getPainel() {
        return jpanel;
    }
}
