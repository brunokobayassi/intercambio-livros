package br.com.livros.service;

import br.com.livros.dao.UsuarioDAO;
import br.com.livros.model.Usuario;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listarTodos();
    }

    public Usuario autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Email e senha são obrigatórios");
        }
        Usuario usuario = usuarioDAO.autenticar(email, senha);
        if (usuario == null) {
            throw new SecurityException("Email ou senha incorretos");
        }
        return usuario;
    }
}
