package br.com.cerradomarmitas.util;

import br.com.cerradomarmitas.models.Categoria;
import br.com.cerradomarmitas.models.Configuracao;
import br.com.cerradomarmitas.models.Marmita;
import br.com.cerradomarmitas.models.Usuario;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class AppData {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private static final FileManager<Marmita> MARMITAS = new FileManager<>(
            "data/marmitas.csv",
            campos -> {
                boolean formatoNovo = campos.length >= 8;
                int deslocamento = formatoNovo ? 1 : 0;
                return new Marmita(
                        Integer.parseInt(campos[0]),
                        formatoNovo ? Integer.parseInt(campos[1]) : 1,
                        campos[1 + deslocamento],
                        campos[2 + deslocamento],
                        Integer.parseInt(campos[3 + deslocamento]),
                        Double.parseDouble(campos[4 + deslocamento].replace(',', '.')),
                        LocalDate.parse(campos[5 + deslocamento], DATE_FORMAT),
                        campos[6 + deslocamento]
                );
            },
            marmita -> new String[]{
                    String.valueOf(marmita.getId()),
                    String.valueOf(marmita.getUsuarioId()),
                    marmita.getNome(),
                    marmita.getCategoria(),
                    String.valueOf(marmita.getQuantidade()),
                    String.valueOf(marmita.getValor()),
                    marmita.getDataValidade().format(DATE_FORMAT),
                    marmita.getObservacoes()
            }
    );

    private static final FileManager<Categoria> CATEGORIAS = new FileManager<>(
            "data/categorias.csv",
            campos -> new Categoria(Integer.parseInt(campos[0]), campos[1]),
            categoria -> new String[]{String.valueOf(categoria.getId()), categoria.getNome()}
    );

    private static final FileManager<Usuario> USUARIOS = new FileManager<>(
            "data/usuarios.csv",
            campos -> new Usuario(
                    Integer.parseInt(campos[0]),
                    campos[1],
                    campos[2],
                    campos[3],
                    campos[4],
                    campos[5]
            ),
            usuario -> new String[]{
                    String.valueOf(usuario.getId()),
                    usuario.getNomeCompleto(),
                    usuario.getUsuario(),
                    usuario.getEmail(),
                    usuario.getSenha(),
                    usuario.getCaminhoFoto()
            }
    );

    private static final FileManager<Configuracao> CONFIGURACOES = new FileManager<>(
            "data/configuracoes.csv",
            campos -> new Configuracao(
                    Integer.parseInt(campos[0]),
                    campos[1],
                    campos.length >= 4 ? campos[3] : campos[2]
            ),
            configuracao -> new String[]{
                    String.valueOf(configuracao.getId()),
                    configuracao.getTema(),
                    configuracao.getFontesSistema()
            }
    );

    static {
        MARMITAS.setCabecalho("id;usuarioId;nome;categoria;quantidade;valor;dataValidade;observacoes");
        CATEGORIAS.setCabecalho("id;nome");
        USUARIOS.setCabecalho("id;nomeCompleto;usuario;email;senha;caminhoFoto");
        CONFIGURACOES.setCabecalho("id;tema;fontesSistema");
    }

    private AppData() {
    }

    public static FileManager<Marmita> marmitas() {
        return MARMITAS;
    }

    public static FileManager<Categoria> categorias() {
        return CATEGORIAS;
    }

    public static FileManager<Usuario> usuarios() {
        return USUARIOS;
    }

    public static FileManager<Configuracao> configuracoes() {
        return CONFIGURACOES;
    }
}
