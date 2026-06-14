package br.com.livros.service;

import br.com.livros.dao.LivroDAO;
import br.com.livros.dao.TrocaDAO;
import br.com.livros.model.TrocaDetalhada;

import java.util.List;
import java.util.NoSuchElementException;

public class TrocaService {

    private final TrocaDAO trocaDAO = new TrocaDAO();
    private final LivroDAO livroDAO = new LivroDAO();

    public List<TrocaDetalhada> listarPendentes(int usuarioId) {
        return trocaDAO.listarPendentesDetalhadas(usuarioId);
    }

    public List<TrocaDetalhada> listarPropostas(int usuarioId) {
        return trocaDAO.listarMinhasPropostas(usuarioId);
    }

    public void propor(int livroOferecidoId, int livroRecebidoId, int usuarioId) {
        int donoOferecido = livroDAO.buscarDonoPorLivro(livroOferecidoId);
        if (donoOferecido != usuarioId) {
            throw new SecurityException("O livro oferecido não pertence a você");
        }

        int usuarioSolicitadoId = livroDAO.buscarDonoPorLivro(livroRecebidoId);
        if (usuarioSolicitadoId == -1) {
            throw new NoSuchElementException("Livro desejado não encontrado");
        }

        if (usuarioSolicitadoId == usuarioId) {
            throw new IllegalArgumentException("Você não pode trocar livros com você mesmo");
        }

        boolean ok = trocaDAO.inserirTroca(livroOferecidoId, livroRecebidoId, usuarioId, usuarioSolicitadoId);
        if (!ok) {
            throw new RuntimeException("Erro ao enviar proposta de troca");
        }
    }

    public String responder(int id, String acao) {
        if (!acao.equals("aceitar") && !acao.equals("recusar")) {
            throw new IllegalArgumentException("Acao deve ser aceitar ou recusar");
        }

        String novoStatus = acao.equals("aceitar") ? "ACEITA" : "RECUSADA";
        boolean ok = trocaDAO.atualizarStatus(id, novoStatus);
        if (!ok) {
            throw new NoSuchElementException("Troca não encontrada");
        }

        return novoStatus.equals("ACEITA") ? "Troca aceita com sucesso" : "Troca recusada com sucesso";
    }
}
