package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Categoria;
import br.com.cerradomarmitas.models.Marmita;
import br.com.cerradomarmitas.util.AppData;
import br.com.cerradomarmitas.util.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class Category {
    private final JPanel jpanel = new JPanel(new BorderLayout(8, 8));
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Nome"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final Runnable aoAlterar;
    private List<Categoria> categorias;

    public Category() {
        this(null);
    }

    public Category(Runnable aoAlterar) {
        this.aoAlterar = aoAlterar;
        criarTela();
        carregarTabela();
    }

    private void criarTela() {
        jpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titulo = new JLabel("Lista de Categorias");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        jpanel.add(titulo, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        jpanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton adicionarButton = new JButton("Adicionar");
        JButton editarButton = new JButton("Editar");
        JButton removerButton = new JButton("Remover");
        JButton fecharButton = new JButton("Fechar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(adicionarButton);
        botoes.add(editarButton);
        botoes.add(removerButton);
        botoes.add(fecharButton);
        jpanel.add(botoes, BorderLayout.SOUTH);

        adicionarButton.addActionListener(e -> adicionar());
        editarButton.addActionListener(e -> editar());
        removerButton.addActionListener(e -> remover());
        fecharButton.addActionListener(e -> fechar());
    }

    private void carregarTabela() {
        categorias = AppData.categorias().carregarTodos();
        tableModel.setRowCount(0);
        for (Categoria categoria : categorias) {
            tableModel.addRow(new Object[]{categoria.getId(), categoria.getNome()});
        }
    }

    private void adicionar() {
        String nome = solicitarNome("Nova categoria", "");
        if (nome == null) {
            return;
        }
        if (!validarNome(nome, null)) {
            return;
        }
        AppData.categorias().adicionar(new Categoria(AppData.categorias().proximoId(), nome));
        aposAlteracao();
        JOptionPane.showMessageDialog(jpanel, "Categoria adicionada com sucesso.");
    }

    private void editar() {
        Categoria categoria = getSelecionada();
        if (categoria == null) {
            return;
        }
        String nomeAntigo = categoria.getNome();
        String novoNome = solicitarNome("Editar categoria", nomeAntigo);
        if (novoNome == null || novoNome.equals(nomeAntigo)) {
            return;
        }
        if (!validarNome(novoNome, categoria.getId())) {
            return;
        }

        Categoria atualizada = new Categoria(categoria.getId(), novoNome);
        AppData.categorias().atualizar(c -> c.getId() == categoria.getId(), atualizada);
        List<Marmita> marmitas = AppData.marmitas().carregarTodos();
        boolean houveVinculo = false;
        for (Marmita marmita : marmitas) {
            if (marmita.getCategoria().equalsIgnoreCase(nomeAntigo)) {
                marmita.setCategoria(novoNome);
                houveVinculo = true;
            }
        }
        if (houveVinculo) {
            AppData.marmitas().salvarTodos(marmitas);
        }
        aposAlteracao();
        JOptionPane.showMessageDialog(jpanel, "Categoria atualizada com sucesso.");
    }

    private void remover() {
        Categoria categoria = getSelecionada();
        if (categoria == null) {
            return;
        }
        boolean emUso = AppData.marmitas().carregarTodos().stream()
                .anyMatch(m -> m.getCategoria().equalsIgnoreCase(categoria.getNome()));
        if (emUso) {
            JOptionPane.showMessageDialog(jpanel,
                    "A categoria está vinculada a uma ou mais marmitas e não pode ser removida.",
                    "Categoria em uso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(jpanel,
                "Remover a categoria '" + categoria.getNome() + "'?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta == JOptionPane.YES_OPTION) {
            AppData.categorias().remover(c -> c.getId() == categoria.getId());
            aposAlteracao();
            JOptionPane.showMessageDialog(jpanel, "Categoria removida com sucesso.");
        }
    }

    private String solicitarNome(String titulo, String valorInicial) {
        JTextField campo = new JTextField(valorInicial, 20);
        campo.addKeyListener(Validador.bloqueadorPontoVirgula());
        int resultado = JOptionPane.showConfirmDialog(jpanel, campo, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return resultado == JOptionPane.OK_OPTION ? campo.getText().trim() : null;
    }

    private boolean validarNome(String nome, Integer idIgnorado) {
        if (Validador.campoVazio(nome)) {
            JOptionPane.showMessageDialog(jpanel, "Informe o nome da categoria.",
                    "Dados inválidos", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (Validador.temPontoVirgula(nome)) {
            JOptionPane.showMessageDialog(jpanel, "O caractere ; não é permitido.",
                    "Dados inválidos", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        boolean duplicada = categorias.stream().anyMatch(c ->
                (idIgnorado == null || c.getId() != idIgnorado) && c.getNome().equalsIgnoreCase(nome));
        if (duplicada) {
            JOptionPane.showMessageDialog(jpanel, "Já existe uma categoria com esse nome.",
                    "Categoria duplicada", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private Categoria getSelecionada() {
        int linha = table.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(jpanel, "Selecione uma categoria na tabela.",
                    "Seleção obrigatória", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int linhaModelo = table.convertRowIndexToModel(linha);
        int id = ((Number) tableModel.getValueAt(linhaModelo, 0)).intValue();
        return categorias.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private void aposAlteracao() {
        carregarTabela();
        if (aoAlterar != null) {
            aoAlterar.run();
        }
    }

    private void fechar() {
        Window janela = SwingUtilities.getWindowAncestor(jpanel);
        if (janela != null) {
            janela.dispose();
        }
    }

    public JPanel getPainel() {
        return jpanel;
    }
}
