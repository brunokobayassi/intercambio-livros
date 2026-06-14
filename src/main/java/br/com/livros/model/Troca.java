package br.com.livros.model;

public class Troca {

    private int id;
    private int livroOferecidoId;
    private int livroRecebidoId;
    private int usuarioSolicitanteId;
    private int usuarioSolicitadoId;
    private String status;

    public Troca() {
    }

    public Troca(int id, int livroOferecidoId, int livroRecebidoId,
            int usuarioSolicitanteId, int usuarioSolicitadoId, String status) {
        this.id = id;
        this.livroOferecidoId = livroOferecidoId;
        this.livroRecebidoId = livroRecebidoId;
        this.usuarioSolicitanteId = usuarioSolicitanteId;
        this.usuarioSolicitadoId = usuarioSolicitadoId;
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

    public int getUsuarioSolicitanteId() {
        return usuarioSolicitanteId;
    }

    public void setUsuarioSolicitanteId(int usuarioSolicitanteId) {
        this.usuarioSolicitanteId = usuarioSolicitanteId;
    }

    public int getUsuarioSolicitadoId() {
        return usuarioSolicitadoId;
    }

    public void setUsuarioSolicitadoId(int usuarioSolicitadoId) {
        this.usuarioSolicitadoId = usuarioSolicitadoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Troca{id=" + id + ", livroOferecidoId=" + livroOferecidoId +
                ", livroRecebidoId=" + livroRecebidoId + ", status='" + status + "'}";
    }
}