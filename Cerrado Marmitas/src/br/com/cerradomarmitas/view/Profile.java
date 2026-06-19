package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Usuario;
import br.com.cerradomarmitas.util.AppData;
import br.com.cerradomarmitas.util.ImagemPerfil;
import br.com.cerradomarmitas.util.Sessao;
import br.com.cerradomarmitas.util.Validador;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class Profile {
    private final JPanel jpanel = new JPanel(new BorderLayout(10, 10));
    private final JTextField nomeTextField = new JTextField(24);
    private final JTextField usuarioTextField = new JTextField(24);
    private final JTextField emailTextField = new JTextField(24);
    private final JPasswordField senhaAtualField = new JPasswordField(18);
    private final JPasswordField novaSenhaField = new JPasswordField(18);
    private final JLabel fotoLabel = new JLabel("Sem foto", SwingConstants.CENTER);
    private final Runnable aoAtualizar;
    private Usuario usuario;

    public Profile(Runnable aoAtualizar) {
        this.aoAtualizar = aoAtualizar;
        this.usuario = Sessao.getUsuario();
        criarTela();
        preencher();
    }

    private void criarTela() {
        jpanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel dados = new JPanel(new GridBagLayout());
        dados.setBorder(BorderFactory.createTitledBorder("Perfil do Usuário"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fotoLabel.setPreferredSize(new Dimension(130, 130));
        fotoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        dados.add(fotoLabel, gbc);
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        adicionarLinha(dados, gbc, "Nome completo:", nomeTextField, 0);
        adicionarLinha(dados, gbc, "Usuário:", usuarioTextField, 1);
        adicionarLinha(dados, gbc, "E-mail:", emailTextField, 2);

        JButton atualizarButton = new JButton("Atualizar Dados");
        JButton fotoButton = new JButton("Alterar Foto");
        JPanel acoesDados = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acoesDados.add(fotoButton);
        acoesDados.add(atualizarButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        dados.add(acoesDados, gbc);

        JPanel senhaPanel = new JPanel(new GridBagLayout());
        senhaPanel.setBorder(BorderFactory.createTitledBorder("Alterar Senha"));
        GridBagConstraints senhaGbc = new GridBagConstraints();
        senhaGbc.insets = new Insets(5, 6, 5, 6);
        senhaGbc.fill = GridBagConstraints.HORIZONTAL;
        adicionarLinhaSenha(senhaPanel, senhaGbc, "Senha atual:", senhaAtualField, 0);
        adicionarLinhaSenha(senhaPanel, senhaGbc, "Nova senha:", novaSenhaField, 1);
        JButton senhaButton = new JButton("Alterar Senha");
        senhaGbc.gridx = 1;
        senhaGbc.gridy = 2;
        senhaPanel.add(senhaButton, senhaGbc);

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(dados, BorderLayout.CENTER);
        centro.add(senhaPanel, BorderLayout.SOUTH);
        jpanel.add(centro, BorderLayout.CENTER);

        JButton fecharButton = new JButton("Fechar");
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(fecharButton);
        jpanel.add(rodape, BorderLayout.SOUTH);

        nomeTextField.addKeyListener(Validador.bloqueadorPontoVirgula());
        usuarioTextField.addKeyListener(Validador.bloqueadorPontoVirgula());
        emailTextField.addKeyListener(Validador.bloqueadorPontoVirgula());
        senhaAtualField.addKeyListener(Validador.bloqueadorPontoVirgula());
        novaSenhaField.addKeyListener(Validador.bloqueadorPontoVirgula());
        atualizarButton.addActionListener(e -> atualizarDados());
        fotoButton.addActionListener(e -> alterarFoto());
        senhaButton.addActionListener(e -> alterarSenha());
        fecharButton.addActionListener(e -> fechar());
    }

    private void adicionarLinha(JPanel panel, GridBagConstraints gbc, String texto, JComponent campo, int linha) {
        gbc.gridx = 1;
        gbc.gridy = linha;
        gbc.weightx = 0;
        panel.add(new JLabel(texto), gbc);
        gbc.gridx = 2;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void adicionarLinhaSenha(JPanel panel, GridBagConstraints gbc, String texto, JComponent campo, int linha) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.weightx = 0;
        panel.add(new JLabel(texto), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void preencher() {
        nomeTextField.setText(usuario.getNomeCompleto());
        usuarioTextField.setText(usuario.getUsuario());
        emailTextField.setText(usuario.getEmail());
        atualizarFoto();
    }

    private void atualizarDados() {
        String nome = nomeTextField.getText().trim();
        String login = usuarioTextField.getText().trim();
        String email = emailTextField.getText().trim();
        if (Validador.campoVazio(nome) || Validador.campoVazio(login) || Validador.campoVazio(email)) {
            erro("Preencha todos os campos obrigatórios.");
            return;
        }
        if (Validador.temPontoVirgula(nome, login, email)) {
            erro("O caractere ; não é permitido.");
            return;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            erro("Informe um e-mail válido.");
            return;
        }
        List<Usuario> usuarios = AppData.usuarios().carregarTodos();
        boolean duplicado = usuarios.stream().anyMatch(u -> u.getId() != usuario.getId()
                && (u.getUsuario().equalsIgnoreCase(login) || u.getEmail().equalsIgnoreCase(email)));
        if (duplicado) {
            erro("Usuário ou e-mail já cadastrado.");
            return;
        }
        usuario.setNomeCompleto(nome);
        usuario.setUsuario(login);
        usuario.setEmail(email);
        salvarUsuario();
        JOptionPane.showMessageDialog(jpanel, "Dados atualizados com sucesso.");
    }

    private void alterarFoto() {
        JFileChooser chooser = new JFileChooser();
        int resultado = chooser.showOpenDialog(jpanel);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String caminho = chooser.getSelectedFile().getAbsolutePath();
        if (Validador.temPontoVirgula(caminho)) {
            erro("O caminho da imagem não pode conter ;.");
            return;
        }
        usuario.setCaminhoFoto(caminho);
        salvarUsuario();
        atualizarFoto();
        JOptionPane.showMessageDialog(jpanel, "Foto atualizada com sucesso.");
    }

    private void atualizarFoto() {
        ImagemPerfil.aplicar(fotoLabel, usuario.getCaminhoFoto(), 125);
    }

    private void alterarSenha() {
        char[] atualChars = senhaAtualField.getPassword();
        char[] novaChars = novaSenhaField.getPassword();
        String atual = new String(atualChars);
        String nova = new String(novaChars);
        Arrays.fill(atualChars, '\0');
        Arrays.fill(novaChars, '\0');
        if (Validador.campoVazio(atual) || Validador.campoVazio(nova)) {
            erro("Informe a senha atual e a nova senha.");
            return;
        }
        if (Validador.temPontoVirgula(atual, nova)) {
            erro("O caractere ; não é permitido.");
            return;
        }
        if (!usuario.getSenha().equals(atual)) {
            erro("A senha atual está incorreta.");
            return;
        }
        usuario.setSenha(nova);
        salvarUsuario();
        senhaAtualField.setText("");
        novaSenhaField.setText("");
        JOptionPane.showMessageDialog(jpanel, "Senha alterada com sucesso.");
    }

    private void salvarUsuario() {
        AppData.usuarios().atualizar(u -> u.getId() == usuario.getId(), usuario);
        Sessao.iniciar(usuario);
        if (aoAtualizar != null) {
            aoAtualizar.run();
        }
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(jpanel, mensagem, "Dados inválidos", JOptionPane.WARNING_MESSAGE);
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
