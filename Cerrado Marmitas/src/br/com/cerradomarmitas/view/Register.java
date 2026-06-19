package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Usuario;
import br.com.cerradomarmitas.util.AppData;
import br.com.cerradomarmitas.util.Validador;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.util.Arrays;
import java.util.List;

public class Register {
    private final JPanel jpanel = new JPanel(new GridBagLayout());
    private final JTextField nomeTextField = new JTextField(25);
    private final JTextField usuarioTextField = new JTextField(25);
    private final JTextField emailTextField = new JTextField(25);
    private final JPasswordField senhaPasswordField = new JPasswordField(25);

    public Register() {
        criarTela();
    }

    private void criarTela() {
        jpanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Criar Nova Conta"),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        adicionarLinha(formulario, gbc, "Nome completo:", nomeTextField, 0);
        adicionarLinha(formulario, gbc, "Usuário:", usuarioTextField, 1);
        adicionarLinha(formulario, gbc, "E-mail:", emailTextField, 2);
        adicionarLinha(formulario, gbc, "Senha:", senhaPasswordField, 3);

        JButton voltarButton = new JButton("Voltar para o Login");
        JButton cadastrarButton = new JButton("Cadastrar");
        JPanel botoes = new JPanel(new BorderLayout());
        botoes.add(voltarButton, BorderLayout.WEST);
        botoes.add(cadastrarButton, BorderLayout.EAST);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formulario.add(botoes, gbc);
        GridBagConstraints painelGbc = new GridBagConstraints();
        painelGbc.gridx = 0;
        painelGbc.gridy = 0;
        painelGbc.weightx = 1;
        painelGbc.weighty = 1;
        painelGbc.fill = GridBagConstraints.BOTH;
        jpanel.add(formulario, painelGbc);

        KeyAdapter bloqueador = Validador.bloqueadorPontoVirgula();
        bloquearPontoVirgula(bloqueador, nomeTextField, usuarioTextField,
                emailTextField, senhaPasswordField);
        voltarButton.addActionListener(e -> voltarLogin());
        cadastrarButton.addActionListener(e -> cadastrar());
        senhaPasswordField.addActionListener(e -> cadastrar());
    }

    private void adicionarLinha(JPanel panel, GridBagConstraints gbc, String label,
                                JComponent campo, int linha) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
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

    private void cadastrar() {
        String nome = nomeTextField.getText().trim();
        String usuario = usuarioTextField.getText().trim();
        String email = emailTextField.getText().trim();
        char[] senhaChars = senhaPasswordField.getPassword();
        String senha = new String(senhaChars);
        Arrays.fill(senhaChars, '\0');

        if (Validador.campoVazio(nome) || Validador.campoVazio(usuario)
                || Validador.campoVazio(email) || Validador.campoVazio(senha)) {
            mostrarErro("Preencha todos os campos obrigatórios.");
            return;
        }
        if (Validador.temPontoVirgula(nome, usuario, email, senha)) {
            mostrarErro("O caractere ; não é permitido.");
            return;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            mostrarErro("Informe um e-mail válido.");
            return;
        }

        List<Usuario> usuarios = AppData.usuarios().carregarTodos();
        if (usuarios.stream().anyMatch(u -> u.getUsuario().equalsIgnoreCase(usuario))) {
            mostrarErro("Esse nome de usuário já está cadastrado.");
            return;
        }
        if (usuarios.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email))) {
            mostrarErro("Esse e-mail já está cadastrado.");
            return;
        }

        Usuario novo = new Usuario(AppData.usuarios().proximoId(), nome, usuario, email, senha, "");
        AppData.usuarios().adicionar(novo);
        JOptionPane.showMessageDialog(jpanel, "Conta criada com sucesso. Faça o login.");
        voltarLogin();
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(jpanel, mensagem, "Não foi possível cadastrar",
                JOptionPane.WARNING_MESSAGE);
    }

    private void voltarLogin() {
        JFrame janela = (JFrame) SwingUtilities.getWindowAncestor(jpanel);
        janela.setTitle("Login - Cerrado Marmitas");
        janela.setContentPane(new Login().getPainel());
        janela.setMinimumSize(new Dimension(480, 400));
        janela.setSize(520, 450);
        janela.setLocationRelativeTo(null);
        janela.revalidate();
        janela.repaint();
    }

    public JPanel getPainel() {
        return jpanel;
    }
}
