package br.com.livros.model;

public class TrocaDetalhada {
    private int id;
    private String tituloLivroOferecido;
    private String tituloLivroRecebido;
    private String nomeProponente;
    private String nomeSolicitado;
    private String status;

    public TrocaDetalhada() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTituloLivroOferecido() {
        return tituloLivroOferecido;
    }

    public void setTituloLivroOferecido(String t) {
        this.tituloLivroOferecido = t;
    }

    public String getTituloLivroRecebido() {
        return tituloLivroRecebido;
    }

    public void setTituloLivroRecebido(String t) {
        this.tituloLivroRecebido = t;
    }

    public String getNomeProponente() {
        return nomeProponente;
    }

    public void setNomeProponente(String n) {
        this.nomeProponente = n;
    }

    public String getNomeSolicitado() {
        return nomeSolicitado;
    }

    public void setNomeSolicitado(String n) {
        this.nomeSolicitado = n;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}