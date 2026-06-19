package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Usuario;
import br.com.cerradomarmitas.util.AppData;
import br.com.cerradomarmitas.util.LoginPersistente;
import br.com.cerradomarmitas.util.Sessao;
import br.com.cerradomarmitas.util.Validador;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Login {
    private final JPanel jpanel = new JPanel(new GridBagLayout());
    private final JTextField usuarioTextField = new JTextField(24);
    private final JPasswordField senhaPasswordField = new JPasswordField(24);
    private final JCheckBox lembrarCheckBox = new JCheckBox("Lembre-se de mim");

    public Login() {
        criarTela();
    }

    private void criarTela() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Acesso ao Sistema"),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titulo = new JLabel("Cerrado Marmitas", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        formulario.add(titulo, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        formulario.add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1;
        formulario.add(usuarioTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formulario.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        formulario.add(senhaPasswordField, gbc);

        JButton entrarButton = new JButton("Entrar");
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formulario.add(entrarButton, gbc);

        gbc.gridy++;
        formulario.add(lembrarCheckBox, gbc);

        JButton cadastrarButton = new JButton("Criar nova conta");
        cadastrarButton.setBorderPainted(false);
        cadastrarButton.setContentAreaFilled(false);
        cadastrarButton.setForeground(new Color(30, 90, 180));
        cadastrarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy++;
        formulario.add(cadastrarButton, gbc);

        jpanel.add(formulario);

        usuarioTextField.addKeyListener(Validador.bloqueadorPontoVirgula());
        senhaPasswordField.addKeyListener(Validador.bloqueadorPontoVirgula());
        entrarButton.addActionListener(e -> autenticar());
        senhaPasswordField.addActionListener(e -> autenticar());
        cadastrarButton.addActionListener(e -> abrirCadastro());
    }

    private void autenticar() {
        String usuario = usuarioTextField.getText().trim();
        char[] senhaChars = senhaPasswordField.getPassword();
        String senha = new String(senhaChars);
        Arrays.fill(senhaChars, '\0');

        if (Validador.campoVazio(usuario) || Validador.campoVazio(senha)) {
            JOptionPane.showMessageDialog(jpanel, "Informe o usuário e a senha.",
                    "Campos obrigatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Validador.temPontoVirgula(usuario, senha)) {
            JOptionPane.showMessageDialog(jpanel, "O caractere ; não é permitido.",
                    "Dados inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario encontrado = AppData.usuarios().carregarTodos().stream()
                .filter(u -> u.getUsuario().equalsIgnoreCase(usuario) && u.getSenha().equals(senha))
                .findFirst()
                .orElse(null);
        if (encontrado == null) {
            senhaPasswordField.setText("");
            JOptionPane.showMessageDialog(jpanel, "Usuário ou senha inválidos.",
                    "Acesso negado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Sessao.iniciar(encontrado);
        if (lembrarCheckBox.isSelected()) {
            LoginPersistente.salvar(encontrado);
        } else {
            LoginPersistente.limpar();
        }
        JFrame janela = getJanela();
        janela.setTitle("Estoque - Cerrado Marmitas");
        janela.setContentPane(new Supply().getPainel());
        janela.setMinimumSize(new Dimension(900, 550));
        janela.setSize(1050, 650);
        janela.setLocationRelativeTo(null);
        janela.revalidate();
        janela.repaint();
    }

    private void abrirCadastro() {
        JFrame janela = getJanela();
        janela.setTitle("Cadastro - Cerrado Marmitas");
        janela.setContentPane(new Register().getPainel());
        janela.setMinimumSize(new Dimension(520, 470));
        janela.setSize(560, 520);
        janela.setLocationRelativeTo(null);
        janela.revalidate();
        janela.repaint();
    }

    private JFrame getJanela() {
        return (JFrame) SwingUtilities.getWindowAncestor(jpanel);
    }

    public JPanel getPainel() {
        return jpanel;
    }
}
