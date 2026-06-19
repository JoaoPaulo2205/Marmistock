package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Categoria;
import br.com.cerradomarmitas.models.Marmita;
import br.com.cerradomarmitas.util.AppData;
import br.com.cerradomarmitas.util.ImagemPerfil;
import br.com.cerradomarmitas.util.LoginPersistente;
import br.com.cerradomarmitas.util.Sessao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Supply {
    private final JPanel jpanel = new JPanel(new BorderLayout(10, 10));
    private final JTextField buscaTextField = new JTextField(20);
    private final JComboBox<String> categoriaComboBox = new JComboBox<>();
    private final JComboBox<String> statusComboBox = new JComboBox<>(
            new String[]{"Todos", "OK", "Próxima do vencimento", "Vencida"});
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Categoria", "Quantidade", "Valor", "Validade", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 3) {
                return Integer.class;
            }
            return String.class;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel totalLabel = criarValorIndicador();
    private final JLabel proximasLabel = criarValorIndicador();
    private final JLabel vencidasLabel = criarValorIndicador();
    private final JLabel categoriasLabel = criarValorIndicador();
    private final JLabel usuarioLabel = new JLabel();
    private final JLabel fotoUsuarioLabel = new JLabel();
    private List<Marmita> marmitas = new ArrayList<>();

    public Supply() {
        criarTela();
        atualizarDados();
    }

    private void criarTela() {
        jpanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topo = new JPanel(new BorderLayout(8, 8));
        JLabel titulo = new JLabel("Estoque de Marmitas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        JButton perfilButton = new JButton("Perfil");
        JButton configuracoesButton = new JButton("Configurações");
        JButton logoutButton = new JButton("Logout");
        JPanel usuarioPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        usuarioPanel.add(usuarioLabel);
        usuarioPanel.add(fotoUsuarioLabel);
        usuarioPanel.add(perfilButton);
        usuarioPanel.add(configuracoesButton);
        usuarioPanel.add(logoutButton);
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(usuarioPanel, BorderLayout.EAST);
        topo.add(cabecalho, BorderLayout.NORTH);
        atualizarNomeUsuario();

        JPanel indicadores = new JPanel(new GridLayout(1, 4, 8, 0));
        indicadores.add(criarIndicador("Total de Marmitas", totalLabel));
        indicadores.add(criarIndicador("Próximas a Vencer", proximasLabel));
        indicadores.add(criarIndicador("Vencidas", vencidasLabel));
        indicadores.add(criarIndicador("Categorias", categoriasLabel));
        topo.add(indicadores, BorderLayout.CENTER);
        jpanel.add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton buscarButton = new JButton("Buscar");
        JButton atualizarButton = new JButton("Atualizar");
        JButton categoriasButton = new JButton("Categorias");
        filtros.add(new JLabel("Buscar:"));
        filtros.add(buscaTextField);
        filtros.add(new JLabel("Categoria:"));
        filtros.add(categoriaComboBox);
        filtros.add(new JLabel("Status:"));
        filtros.add(statusComboBox);
        filtros.add(buscarButton);
        filtros.add(atualizarButton);
        filtros.add(categoriasButton);
        centro.add(filtros, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(3).setPreferredWidth(75);
        centro.add(new JScrollPane(table), BorderLayout.CENTER);
        jpanel.add(centro, BorderLayout.CENTER);

        JButton adicionarButton = new JButton("Adicionar");
        JButton editarButton = new JButton("Editar");
        JButton removerButton = new JButton("Remover");
        JButton detalhesButton = new JButton("Ver Detalhes");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(adicionarButton);
        botoes.add(editarButton);
        botoes.add(removerButton);
        botoes.add(detalhesButton);
        jpanel.add(botoes, BorderLayout.SOUTH);

        buscarButton.addActionListener(e -> aplicarFiltros());
        buscaTextField.addActionListener(e -> aplicarFiltros());
        categoriaComboBox.addActionListener(e -> aplicarFiltros());
        statusComboBox.addActionListener(e -> aplicarFiltros());
        atualizarButton.addActionListener(e -> atualizarDados());
        categoriasButton.addActionListener(e -> abrirCategorias());
        adicionarButton.addActionListener(e -> abrirFormulario(null));
        editarButton.addActionListener(e -> editarSelecionada());
        removerButton.addActionListener(e -> removerSelecionada());
        detalhesButton.addActionListener(e -> mostrarDetalhes());
        perfilButton.addActionListener(e -> abrirPerfil());
        configuracoesButton.addActionListener(e -> abrirConfiguracoes());
        logoutButton.addActionListener(e -> logout());
        table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "detalhes");
        table.getActionMap().put("detalhes", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                mostrarDetalhes();
            }
        });
    }

    private static JLabel criarValorIndicador() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        return label;
    }

    private static Color corVermelha() {
        return new Color(220, 55, 55);
    }

    private static Color corLaranja() {
        return new Color(230, 130, 20);
    }

    private static Color corVerde() {
        return new Color(35, 155, 75);
    }

    private JPanel criarIndicador(String titulo, JLabel valor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        panel.add(new JLabel(titulo, SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);
        return panel;
    }

    public void atualizarDados() {
        try {
            int usuarioId = Sessao.getUsuario() == null ? -1 : Sessao.getUsuario().getId();
            marmitas = AppData.marmitas().carregarTodos().stream()
                    .filter(marmita -> marmita.getUsuarioId() == usuarioId)
                    .collect(Collectors.toList());
            carregarCategoriasFiltro();
            atualizarIndicadores();
            aplicarFiltros();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(jpanel, e.getMessage(), "Erro ao carregar CSV",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarCategoriasFiltro() {
        Object selecionada = categoriaComboBox.getSelectedItem();
        categoriaComboBox.removeAllItems();
        categoriaComboBox.addItem("Todas");
        for (Categoria categoria : AppData.categorias().carregarTodos()) {
            categoriaComboBox.addItem(categoria.getNome());
        }
        if (selecionada != null) {
            categoriaComboBox.setSelectedItem(selecionada);
        }
    }

    private void atualizarIndicadores() {
        long proximas = marmitas.stream()
                .filter(m -> "Próxima do vencimento".equals(statusDa(m.getDataValidade())))
                .count();
        long vencidas = marmitas.stream()
                .filter(m -> "Vencida".equals(statusDa(m.getDataValidade())))
                .count();
        int totalUnidades = marmitas.stream().mapToInt(Marmita::getQuantidade).sum();
        totalLabel.setText(String.valueOf(totalUnidades));
        proximasLabel.setText(String.valueOf(proximas));
        vencidasLabel.setText(String.valueOf(vencidas));
        categoriasLabel.setText(String.valueOf(AppData.categorias().contar()));
        totalLabel.setForeground(totalUnidades <= 5
                ? corVermelha()
                : totalUnidades <= 14 ? corLaranja() : corVerde());
        proximasLabel.setForeground(corLaranja());
        vencidasLabel.setForeground(corVermelha());
    }

    private void aplicarFiltros() {
        String busca = buscaTextField.getText().trim().toLowerCase(Locale.ROOT);
        String categoria = (String) categoriaComboBox.getSelectedItem();
        String status = (String) statusComboBox.getSelectedItem();

        tableModel.setRowCount(0);
        for (Marmita marmita : marmitas) {
            String statusMarmita = statusDa(marmita.getDataValidade());
            boolean correspondeBusca = busca.isEmpty()
                    || marmita.getNome().toLowerCase(Locale.ROOT).contains(busca)
                    || marmita.getCategoria().toLowerCase(Locale.ROOT).contains(busca)
                    || marmita.getObservacoes().toLowerCase(Locale.ROOT).contains(busca);
            boolean correspondeCategoria = categoria == null || "Todas".equals(categoria)
                    || categoria.equalsIgnoreCase(marmita.getCategoria());
            boolean correspondeStatus = status == null || "Todos".equals(status)
                    || status.equals(statusMarmita);
            if (correspondeBusca && correspondeCategoria && correspondeStatus) {
                tableModel.addRow(new Object[]{
                        marmita.getId(),
                        marmita.getNome(),
                        marmita.getCategoria(),
                        marmita.getQuantidade(),
                        String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", marmita.getValor()),
                        marmita.getDataValidade().format(AppData.DATE_FORMAT),
                        statusMarmita
                });
            }
        }
    }

    public static String statusDa(LocalDate validade) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), validade);
        if (dias < 0) {
            return "Vencida";
        }
        if (dias <= 7) {
            return "Próxima do vencimento";
        }
        return "OK";
    }

    private Marmita getSelecionada() {
        int linha = table.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(jpanel, "Selecione uma marmita na tabela.",
                    "Seleção obrigatória", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int linhaModelo = table.convertRowIndexToModel(linha);
        int id = ((Number) tableModel.getValueAt(linhaModelo, 0)).intValue();
        return marmitas.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }

    private void abrirFormulario(Marmita marmita) {
        Window owner = SwingUtilities.getWindowAncestor(jpanel);
        SupplyRegister formulario = new SupplyRegister(marmita, this::atualizarDados);
        JDialog dialog = new JDialog(owner,
                marmita == null ? "Cadastrar Marmita" : "Editar Marmita",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(formulario.getPainel());
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(700, 560);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void editarSelecionada() {
        Marmita marmita = getSelecionada();
        if (marmita != null) {
            abrirFormulario(marmita);
        }
    }

    private void removerSelecionada() {
        Marmita marmita = getSelecionada();
        if (marmita == null) {
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(jpanel,
                "Remover a marmita '" + marmita.getNome() + "'?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta == JOptionPane.YES_OPTION && AppData.marmitas().remover(
                m -> m.getId() == marmita.getId() && m.getUsuarioId() == marmita.getUsuarioId())) {
            atualizarDados();
            JOptionPane.showMessageDialog(jpanel, "Marmita removida com sucesso.");
        }
    }

    private void mostrarDetalhes() {
        Marmita marmita = getSelecionada();
        if (marmita == null) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(jpanel);
        SupplyDetails detalhes = new SupplyDetails(marmita);
        JDialog dialog = new JDialog(owner, "Detalhes da Marmita", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(detalhes.getPainel());
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void abrirCategorias() {
        Window owner = SwingUtilities.getWindowAncestor(jpanel);
        Category category = new Category(this::atualizarDados);
        JDialog dialog = new JDialog(owner, "Categorias - Cerrado Marmitas",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(category.getPainel());
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        atualizarDados();
    }

    private void logout() {
        int resposta = JOptionPane.showConfirmDialog(jpanel,
                "Deseja sair da conta atual?", "Confirmar logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        Sessao.encerrar();
        LoginPersistente.limpar();
        JFrame janela = (JFrame) SwingUtilities.getWindowAncestor(jpanel);
        janela.setTitle("Login - Cerrado Marmitas");
        janela.setContentPane(new Login().getPainel());
        janela.setMinimumSize(new Dimension(480, 400));
        janela.setSize(520, 450);
        janela.setLocationRelativeTo(null);
        janela.revalidate();
        janela.repaint();
    }

    private void abrirPerfil() {
        if (Sessao.getUsuario() == null) {
            JOptionPane.showMessageDialog(jpanel, "Nenhum usuário autenticado.",
                    "Perfil indisponível", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(jpanel);
        Profile profile = new Profile(this::atualizarNomeUsuario);
        JDialog dialog = new JDialog(owner, "Perfil do Usuário", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(profile.getPainel());
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(680, 570);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void abrirConfiguracoes() {
        Window owner = SwingUtilities.getWindowAncestor(jpanel);
        Settings settings = new Settings();
        JDialog dialog = new JDialog(owner, "Configurações", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(settings.getPainel());
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(460, 320);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void atualizarNomeUsuario() {
        usuarioLabel.setText(Sessao.getUsuario() == null
                ? "Usuário"
                : Sessao.getUsuario().getNomeCompleto());
        ImagemPerfil.aplicar(fotoUsuarioLabel,
                Sessao.getUsuario() == null ? null : Sessao.getUsuario().getCaminhoFoto(), 32);
    }

    public JPanel getPainel() {
        return jpanel;
    }
}
