package br.com.livros.service;

import br.com.livros.dao.LivroDAO;
import br.com.livros.model.Livro;

import java.util.List;
import java.util.NoSuchElementException;

public class LivroService {

    private final LivroDAO livroDAO = new LivroDAO();

    public List<Livro> listarDeOutrosUsuarios(int usuarioId) {
        return livroDAO.listarDeOutrosUsuarios(usuarioId);
    }

    public Livro buscarPorId(int id) {
        Livro livro = livroDAO.buscarPorId(id);
        if (livro == null) {
            throw new NoSuchElementException("Livro não encontrado");
        }
        return livro;
    }

    public void cadastrar(Livro livro, int usuarioId) {
        livro.setUsuarioId(usuarioId);
        boolean ok = livroDAO.cadastrarLivro(livro);
        if (!ok) {
            throw new RuntimeException("Erro ao cadastrar livro");
        }
    }

    public void atualizar(int id, Livro dados, int usuarioId) {
        Livro existente = livroDAO.buscarPorId(id);
        if (existente == null) {
            throw new NoSuchElementException("Livro não encontrado");
        }
        if (existente.getUsuarioId() != usuarioId) {
            throw new SecurityException("Você não tem permissão para editar este livro");
        }
        dados.setId(id);
        dados.setUsuarioId(usuarioId);
        boolean ok = livroDAO.atualizar(dados);
        if (!ok) {
            throw new RuntimeException("Erro ao atualizar livro");
        }
    }

    public void deletar(int id, int usuarioId) {
        Livro livro = livroDAO.buscarPorId(id);
        if (livro == null) {
            throw new NoSuchElementException("Livro não encontrado");
        }
        if (livro.getUsuarioId() != usuarioId) {
            throw new SecurityException("Você não tem permissão para deletar este livro");
        }
        boolean ok = livroDAO.deletar(id);
        if (!ok) {
            throw new RuntimeException("Erro ao deletar livro");
        }
    }
}
