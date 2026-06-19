package br.com.cerradomarmitas.models;

public class Configuracao {
    private int id;
    private String tema;
    private String fontesSistema;

    public Configuracao() {}

    public Configuracao(int id, String tema, String fontesSistema) {
        this.id = id;
        this.tema = tema;
        this.fontesSistema = fontesSistema;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public String getFontesSistema() { return fontesSistema; }
    public void setFontesSistema(String fontesSistema) { this.fontesSistema = fontesSistema; }
}
