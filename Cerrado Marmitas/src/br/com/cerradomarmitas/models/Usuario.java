package br.com.cerradomarmitas.models;

public class Usuario {
    private int id;
    private String nomeCompleto;
    private String usuario;
    private String email;
    private String senha;
    private String caminhoFoto;

    public Usuario() {}

    public Usuario(int id, String nomeCompleto, String usuario, String email, String senha, String caminhoFoto) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.usuario = usuario;
        this.email = email;
        this.senha = senha;
        this.caminhoFoto = caminhoFoto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getCaminhoFoto() { return caminhoFoto; }
    public void setCaminhoFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; }

    @Override
    public String toString() {
        return nomeCompleto;
    }
}
