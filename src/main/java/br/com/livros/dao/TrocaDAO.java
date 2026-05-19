package br.com.livros.dao;

import br.com.livros.model.Troca;
import br.com.livros.model.TrocaDetalhada;
import br.com.livros.config.MysqlSingleton;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TrocaDAO {

    public boolean atualizarStatus(int trocaId, String novoStatus) {
        String sqlUpdate = "UPDATE trocas SET status = ? WHERE id = ?";

        try {
            int linhas = MysqlSingleton.getInstance().executarUpdate(sqlUpdate, novoStatus, trocaId);

            if (linhas > 0 && novoStatus.equals("ACEITA")) {
                // Busca os ids dos livros envolvidos
                String sqlSelect = "SELECT livro_oferecido_id, livro_recebido_id FROM trocas WHERE id = ?";
                try (ResultSet rs = MysqlSingleton.getInstance().executar(sqlSelect, trocaId)) {
                    if (rs.next()) {
                        int livroOferecidoId = rs.getInt("livro_oferecido_id");
                        int livroRecebidoId = rs.getInt("livro_recebido_id");

                        // Busca donos atuais
                        String sqlDonos = "SELECT id, usuario_id FROM livros WHERE id IN (?, ?)";
                        try (ResultSet rsDonos = MysqlSingleton.getInstance().executar(sqlDonos, livroOferecidoId,
                                livroRecebidoId)) {
                            int donoOferecido = 0, donoRecebido = 0;
                            while (rsDonos.next()) {
                                if (rsDonos.getInt("id") == livroOferecidoId) {
                                    donoOferecido = rsDonos.getInt("usuario_id");
                                } else {
                                    donoRecebido = rsDonos.getInt("usuario_id");
                                }
                            }

                            // Troca os donos
                            MysqlSingleton.getInstance().executarUpdate(
                                    "UPDATE livros SET usuario_id = ? WHERE id = ?", donoRecebido, livroOferecidoId);
                            MysqlSingleton.getInstance().executarUpdate(
                                    "UPDATE livros SET usuario_id = ? WHERE id = ?", donoOferecido, livroRecebidoId);
                        }
                    }
                }
            }
            return linhas > 0;
        } catch (Exception e) {
            System.out.println("Erro ao atualizar status da troca: " + e.getMessage());
            return false;
        }
    }

    // Trocas recebidas — onde o usuário logado é o solicitado
    public List<TrocaDetalhada> listarPendentesDetalhadas(int idUsuarioLogado) {
        List<TrocaDetalhada> pendentes = new ArrayList<>();

        String sql = "SELECT t.id, " +
                "  lo.titulo  AS titulo_oferecido, " +
                "  lr.titulo  AS titulo_recebido, " +
                "  us.nome    AS nome_solicitante, " +
                "  ud.nome    AS nome_solicitado, " +
                "  t.status " +
                "FROM trocas t " +
                "INNER JOIN livros lo   ON t.livro_oferecido_id    = lo.id " +
                "INNER JOIN livros lr   ON t.livro_recebido_id     = lr.id " +
                "INNER JOIN usuarios us ON t.usuario_solicitante_id = us.id " +
                "INNER JOIN usuarios ud ON t.usuario_solicitado_id  = ud.id " +
                "WHERE t.usuario_solicitado_id = ? AND t.status = 'PENDENTE'";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, idUsuarioLogado)) {
            while (rs.next()) {
                TrocaDetalhada t = new TrocaDetalhada();
                t.setId(rs.getInt("id"));
                t.setTituloLivroOferecido(rs.getString("titulo_oferecido"));
                t.setTituloLivroRecebido(rs.getString("titulo_recebido"));
                t.setNomeProponente(rs.getString("nome_solicitante"));
                t.setNomeSolicitado(rs.getString("nome_solicitado"));
                t.setStatus(rs.getString("status"));
                pendentes.add(t);
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar trocas pendentes: " + e.getMessage());
        }
        return pendentes;
    }

    // Minhas propostas — onde o usuário logado é o solicitante
    public List<TrocaDetalhada> listarMinhasPropostas(int idUsuarioLogado) {
        List<TrocaDetalhada> propostas = new ArrayList<>();

        String sql = "SELECT t.id, " +
                "  lo.titulo  AS titulo_oferecido, " +
                "  lr.titulo  AS titulo_recebido, " +
                "  us.nome    AS nome_solicitante, " +
                "  ud.nome    AS nome_solicitado, " +
                "  t.status " +
                "FROM trocas t " +
                "INNER JOIN livros lo   ON t.livro_oferecido_id    = lo.id " +
                "INNER JOIN livros lr   ON t.livro_recebido_id     = lr.id " +
                "INNER JOIN usuarios us ON t.usuario_solicitante_id = us.id " +
                "INNER JOIN usuarios ud ON t.usuario_solicitado_id  = ud.id " +
                "WHERE t.usuario_solicitante_id = ?";

        try (ResultSet rs = MysqlSingleton.getInstance().executar(sql, idUsuarioLogado)) {
            while (rs.next()) {
                TrocaDetalhada t = new TrocaDetalhada();
                t.setId(rs.getInt("id"));
                t.setTituloLivroOferecido(rs.getString("titulo_oferecido"));
                t.setTituloLivroRecebido(rs.getString("titulo_recebido"));
                t.setNomeProponente(rs.getString("nome_solicitante"));
                t.setNomeSolicitado(rs.getString("nome_solicitado"));
                t.setStatus(rs.getString("status"));
                propostas.add(t);
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar minhas propostas: " + e.getMessage());
        }
        return propostas;
    }

    // Insere troca com usuario_solicitado_id
    public boolean inserirTroca(int livroOferecidoId, int livroRecebidoId,
            int usuarioSolicitanteId, int usuarioSolicitadoId) {
        String sql = "INSERT INTO trocas " +
                "(livro_oferecido_id, livro_recebido_id, usuario_solicitante_id, usuario_solicitado_id, status) " +
                "VALUES (?, ?, ?, ?, 'PENDENTE')";
        try {
            int linhas = MysqlSingleton.getInstance().executarUpdate(
                    sql, livroOferecidoId, livroRecebidoId, usuarioSolicitanteId, usuarioSolicitadoId);
            return linhas > 0;
        } catch (Exception e) {
            System.out.println("Erro ao inserir troca: " + e.getMessage());
            return false;
        }
    }
}