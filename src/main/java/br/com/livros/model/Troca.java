package br.com.livros.model;

public class Troca {
    private int id;
    private int livroOferecidoId;
    private int livroRecebidoId;
    private String status; 

    public Troca() {
    }

    public Troca(int id, int livroOferecidoId, int livroRecebidoId, String status) {
        this.id = id;
        this.livroOferecidoId = livroOferecidoId;
        this.livroRecebidoId = livroRecebidoId;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLivroOferecidoId() {
        return livroOferecidoId;
    }

    public void setLivroOferecidoId(int livroOferecidoId) {
        this.livroOferecidoId = livroOferecidoId;
    }

    public int getLivroRecebidoId() {
        return livroRecebidoId;
    }

    public void setLivroRecebidoId(int livroRecebidoId) {
        this.livroRecebidoId = livroRecebidoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}