package br.com.livros.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.com.livros.config.MysqlSingleton;
import br.com.livros.model.Usuario;

public class UsuarioDAO {

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";

        try {
            ResultSet rs = MysqlSingleton.getInstance().executar(sql);
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                lista.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Usuario autenticar(String email, String senha) {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND senha = ?";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, email, senha)) {
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                return usuario;
            }
        } catch (Exception e) {
            System.out.println("Erro na autenticacao: " + e.getMessage());
        }
        return null;
    }

    // Busca usuário pelo email — usado pelo JWT para validar o token
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, email)) {
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setSenha(rs.getString("senha"));
                return usuario;
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuario por email: " + e.getMessage());
        }
        return null;
    }

    // Busca usuário pelo ID — útil para operações da API
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, id)) {
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                return usuario;
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar usuario por id: " + e.getMessage());
        }
        return null;
    }

    // Cadastra um novo usuário
    public boolean cadastrar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)";

        try {
            int linhas = MysqlSingleton.getInstance().executarUpdate(
                sql, usuario.getNome(), usuario.getEmail(), usuario.getSenha()
            );
            return linhas > 0;
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar usuario: " + e.getMessage());
            return false;
        }
    }

    // Atualiza nome e email do usuário
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, email = ? WHERE id = ?";

        try {
            int linhas = MysqlSingleton.getInstance().executarUpdate(
                sql, usuario.getNome(), usuario.getEmail(), usuario.getId()
            );
            return linhas > 0;
        } catch (Exception e) {
            System.out.println("Erro ao atualizar usuario: " + e.getMessage());
            return false;
        }
    }

    // Deleta um usuário pelo ID
    public boolean deletar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try {
            int linhas = MysqlSingleton.getInstance().executarUpdate(sql, id);
            return linhas > 0;
        } catch (Exception e) {
            System.out.println("Erro ao deletar usuario: " + e.getMessage());
            return false;
        }
    }
}