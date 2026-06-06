package br.com.livros.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.com.livros.config.MysqlSingleton;
import br.com.livros.model.Livro;

public class LivroDAO {

    public List<Livro> listarTodos() {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql)) {
            while (rs.next()) {
                Livro livro = new Livro();
                livro.setId(rs.getInt("id"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setAutor(rs.getString("autor"));
                livro.setUsuarioId(rs.getInt("usuario_id"));
                livros.add(livro);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar livros: " + e.getMessage());
        }
        return livros;
    }

    public List<Livro> listarDeOutrosUsuarios(int idUsuarioLogado) {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT l.id, l.titulo, l.autor, l.usuario_id, u.nome AS nome_dono " +
                "FROM livros l " +
                "INNER JOIN usuarios u ON l.usuario_id = u.id " +
                "WHERE l.usuario_id != ?";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, idUsuarioLogado)) {
            while (rs.next()) {
                Livro livro = new Livro();
                livro.setId(rs.getInt("id"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setAutor(rs.getString("autor"));
                livro.setUsuarioId(rs.getInt("usuario_id"));
                livro.setNomeDono(rs.getString("nome_dono"));
                livros.add(livro);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar livros de outros usuários: " + e.getMessage());
        }
        return livros;
    }

    public List<Livro> listarDoUsuario(int idUsuarioLogado) {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros WHERE usuario_id = ?";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, idUsuarioLogado)) {
            while (rs.next()) {
                Livro livro = new Livro();
                livro.setId(rs.getInt("id"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setAutor(rs.getString("autor"));
                livro.setUsuarioId(rs.getInt("usuario_id"));
                livros.add(livro);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar livros do usuário: " + e.getMessage());
        }
        return livros;
    }

    // Busca um livro pelo ID — usado no UPDATE e DELETE da API
    public Livro buscarPorId(int id) {
        String sql = "SELECT l.id, l.titulo, l.autor, l.usuario_id, u.nome AS nome_dono " +
                "FROM livros l " +
                "INNER JOIN usuarios u ON l.usuario_id = u.id " +
                "WHERE l.id = ?";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, id)) {
            if (rs.next()) {
                Livro livro = new Livro();
                livro.setId(rs.getInt("id"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setAutor(rs.getString("autor"));
                livro.setUsuarioId(rs.getInt("usuario_id"));
                livro.setNomeDono(rs.getString("nome_dono"));
                return livro;
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar livro por id: " + e.getMessage());
        }
        return null;
    }

    // Retorna o usuario_id dono do livro — usado ao propor troca
    public int buscarDonoPorLivro(int livroId) {
        String sql = "SELECT usuario_id FROM livros WHERE id = ?";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, livroId)) {
            if (rs.next()) {
                return rs.getInt("usuario_id");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar dono do livro: " + e.getMessage());
        }
        return -1;
    }

    public boolean cadastrarLivro(Livro livro) {
        String sql = "INSERT INTO livros (titulo, autor, usuario_id) VALUES (?, ?, ?)";

        Object[] parametros = {
                livro.getTitulo(),
                livro.getAutor(),
                livro.getUsuarioId()
        };

        try {
            int linhasAfetadas = MysqlSingleton.getInstance().executarUpdate(sql, parametros);
            return linhasAfetadas > 0;
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar livro: " + e.getMessage());
            return false;
        }
    }

    // Atualiza título e autor do livro — usado no PUT da API
    public boolean atualizar(Livro livro) {
        String sql = "UPDATE livros SET titulo = ?, autor = ? WHERE id = ?";

        try {
            int linhas = MysqlSingleton.getInstance().executarUpdate(
                    sql, livro.getTitulo(), livro.getAutor(), livro.getId());
            return linhas > 0;
        } catch (Exception e) {
            System.out.println("Erro ao atualizar livro: " + e.getMessage());
            return false;
        }
    }

    // Deleta um livro pelo ID — usado no DELETE da API
    public boolean deletar(int id) {
        String sql = "DELETE FROM livros WHERE id = ?";

        try {
            int linhas = MysqlSingleton.getInstance().executarUpdate(sql, id);
            return linhas > 0;
        } catch (Exception e) {
            System.out.println("Erro ao deletar livro: " + e.getMessage());
            return false;
        }
    }
}