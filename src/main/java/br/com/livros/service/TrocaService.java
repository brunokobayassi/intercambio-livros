package br.com.livros.service;

import br.com.livros.model.Livro;

public class TrocaService {

    public boolean validarNovaTroca(Livro livroOferecido, Livro livroRecebido) {

        if (livroOferecido.getUsuarioId() == livroRecebido.getUsuarioId()) {
            System.out.println("Erro de Validação: Você não pode trocar um livro com você mesmo.");
            return false;
        }

        return true;
    }
}