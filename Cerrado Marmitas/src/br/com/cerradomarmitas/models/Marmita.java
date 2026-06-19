package br.com.cerradomarmitas.models;

import java.time.LocalDate;

public class Marmita {
    private int id;
    private int usuarioId;
    private String nome;
    private String categoria;
    private int quantidade;
    private double valor;
    private LocalDate dataValidade;
    private String observacoes;

    public Marmita() {}

    public Marmita(int id, int usuarioId, String nome, String categoria, int quantidade, double valor,
                   LocalDate dataValidade, String observacoes) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.valor = valor;
        this.dataValidade = dataValidade;
        this.observacoes = observacoes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    @Override
    public String toString() {
        return nome;
    }
}
