package br.com.livros.controller;

import br.com.livros.dao.LivroDAO;
import br.com.livros.dao.TrocaDAO;
import br.com.livros.dao.UsuarioDAO;
import br.com.livros.model.TrocaDetalhada;
import br.com.livros.model.Usuario;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/trocas/*")
public class TrocaApiController extends HttpServlet {

    private final Gson gson = new Gson();
    private final TrocaDAO trocaDAO = new TrocaDAO();
    private final LivroDAO livroDAO = new LivroDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ================================================================
    // GET /api/trocas — lista pendentes e minhas propostas
    // GET /api/trocas/pendentes — lista só as pendentes
    // GET /api/trocas/propostas — lista só as minhas propostas
    // ================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = (String) request.getAttribute("emailUsuario");
        Usuario usuarioLogado = usuarioDAO.buscarPorEmail(email);

        String pathInfo = request.getPathInfo();

        // GET /api/trocas/pendentes
        if ("/pendentes".equals(pathInfo)) {
            List<TrocaDetalhada> pendentes = trocaDAO.listarPendentesDetalhadas(usuarioLogado.getId());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(pendentes));
            return;
        }

        // GET /api/trocas/propostas
        if ("/propostas".equals(pathInfo)) {
            List<TrocaDetalhada> propostas = trocaDAO.listarMinhasPropostas(usuarioLogado.getId());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(propostas));
            return;
        }

        // GET /api/trocas — retorna as duas listas juntas
        List<TrocaDetalhada> pendentes = trocaDAO.listarPendentesDetalhadas(usuarioLogado.getId());
        List<TrocaDetalhada> propostas = trocaDAO.listarMinhasPropostas(usuarioLogado.getId());

        JsonObject resposta = new JsonObject();
        resposta.add("pendentes", gson.toJsonTree(pendentes));
        resposta.add("propostas", gson.toJsonTree(propostas));

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(resposta));
    }

    // ================================================================
    // POST /api/trocas — propõe uma nova troca
    // Body: { "livroOferecidoId": 1, "livroRecebidoId": 2 }
    // ================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Lê o body
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                body.append(linha);
            }
        }

        JsonObject dados = gson.fromJson(body.toString(), JsonObject.class);

        // Valida campos obrigatórios
        if (dados == null
                || !dados.has("livroOferecidoId")
                || !dados.has("livroRecebidoId")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"livroOferecidoId e livroRecebidoId são obrigatórios\"}");
            return;
        }

        int livroOferecidoId = dados.get("livroOferecidoId").getAsInt();
        int livroRecebidoId = dados.get("livroRecebidoId").getAsInt();

        // Pega o usuário logado
        String email = (String) request.getAttribute("emailUsuario");
        Usuario usuarioLogado = usuarioDAO.buscarPorEmail(email);

        // Valida se o livro oferecido pertence ao usuário logado
        int donoOferecido = livroDAO.buscarDonoPorLivro(livroOferecidoId);
        if (donoOferecido != usuarioLogado.getId()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"erro\": \"O livro oferecido não pertence a você\"}");
            return;
        }

        // Busca o dono do livro desejado
        int usuarioSolicitadoId = livroDAO.buscarDonoPorLivro(livroRecebidoId);
        if (usuarioSolicitadoId == -1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"erro\": \"Livro desejado não encontrado\"}");
            return;
        }

        // Impede troca consigo mesmo
        if (usuarioSolicitadoId == usuarioLogado.getId()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"Você não pode trocar livros com você mesmo\"}");
            return;
        }

        boolean ok = trocaDAO.inserirTroca(
                livroOferecidoId, livroRecebidoId,
                usuarioLogado.getId(), usuarioSolicitadoId);

        if (ok) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"mensagem\": \"Proposta de troca enviada com sucesso\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\": \"Erro ao enviar proposta de troca\"}");
        }
    }

    // ================================================================
    // PUT /api/trocas/{id} — aceita ou recusa uma troca
    // Body: { "acao": "aceitar" } ou { "acao": "recusar" }
    // ================================================================
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"ID da troca é obrigatório\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            // Lê o body
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    body.append(linha);
                }
            }

            JsonObject dados = gson.fromJson(body.toString(), JsonObject.class);

            if (dados == null || !dados.has("acao")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"Campo acao é obrigatório\"}");
                return;
            }

            String acao = dados.get("acao").getAsString().toLowerCase();

            // Valida a ação
            if (!acao.equals("aceitar") && !acao.equals("recusar")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\": \"Acao deve ser aceitar ou recusar\"}");
                return;
            }

            String novoStatus = acao.equals("aceitar") ? "ACEITA" : "RECUSADA";

            boolean ok = trocaDAO.atualizarStatus(id, novoStatus);

            if (ok) {
                String mensagem = novoStatus.equals("ACEITA")
                        ? "Troca aceita com sucesso"
                        : "Troca recusada com sucesso";
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\": \"" + mensagem + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"erro\": \"Troca não encontrada\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\": \"ID inválido\"}");
        }
    }
}